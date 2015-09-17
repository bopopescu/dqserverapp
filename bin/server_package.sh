#!/bin/bash
# Script for packaging all the job server files to .tar.gz for Mesos or other single-image deploys
WORK_DIR=/tmp/job-server

ENV=$1
if [ -z "$ENV" ]; then
  echo "Syntax: $0 <Environment>"
  echo "   for a list of environments, ls config/*.sh"
  exit 0
fi

export VARIABLE=value

echo Packaging DQ job-server for environment $ENV...

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

if [ -z "$CONFIG_DIR" ]; then
  CONFIG_DIR=`cd "$bin"/../config/; pwd`
fi

configFile="$CONFIG_DIR/$ENV.sh"

echo "Config file is $configFile"

if [ ! -f "$configFile" ]; then
  echo "Could not find $configFile"
  exit 1
fi
. $configFile

echo SPARK_VERSION=$SPARK_VERSION

export SPARK_VERSION=$SPARK_VERSION

majorRegex='([0-9]+\.[0-9]+)\.[0-9]+'
if [[ $SCALA_VERSION =~ $majorRegex ]]
then
  majorVersion="${BASH_REMATCH[1]}"
else
  echo "Please specify SCALA_VERSION in ${configFile}"
  exit 1
fi

cd "$SPARKJOBSERVER_DIR"

echo "Building Spark Job Server ........."

sbt ++$SCALA_VERSION job-server-extras/assembly
if [ "$?" != "0" ]; then
  echo "Assembly failed for Spark Job Server"
  exit 1
fi

echo "Publishing local Spark Job Server ........."

sbt ++$SCALA_VERSION publishLocal
if [ "$?" != "0" ]; then
  echo "Publish Local failed for Spark Job Server"
  exit 1
fi

cd $DQSERVER_DIR/dqjobserver

echo "Building DQ Job Server ........."

SJS_VERSION=$(sed -e 's/^"//' -e 's/"$//' <<< $(awk -F'=' '{ print $2 }' $SPARKJOBSERVER_DIR/version.sbt))
export SJS_VERSION=$SJS_VERSION

echo "SJS_VERSION = $SJS_VERSION"

sbt ++$SCALA_VERSION assembly
if [ "$?" != "0" ]; then
  echo "Assembly failed for DQ Job Server"
  exit 1
fi

FILES="$SPARKJOBSERVER_DIR/job-server-extras/target/scala-$majorVersion/spark-job-server.jar
       $SPARKJOBSERVER_DIR/bin/server_start.sh
       $SPARKJOBSERVER_DIR/bin/server_stop.sh
       $SPARKJOBSERVER_DIR/bin/kill-process-tree.sh
       $CONFIG_DIR/$ENV.conf
       $SPARKJOBSERVER_DIR/config/log4j-server.properties
       $DQSERVER_DIR/dqjobserver/target/DQJobServer.jar"

	   
rm -rf $INSTALL_DIR
mkdir -p $INSTALL_DIR

cp $FILES $INSTALL_DIR/
cp $configFile $INSTALL_DIR/settings.sh

sed -i "s/spark.jobserver.JobServer/com.sar.spark.server.DQJobServer/" $INSTALL_DIR/server_start.sh	
sed -i "s/\/bin\/spark-submit/& --jars \$appdir\/DQJobServer.jar/" $INSTALL_DIR/server_start.sh	

pushd $INSTALL_DIR
TAR_FILE=$INSTALL_DIR/job-server.tar.gz
rm -f $TAR_FILE
tar zcvf $TAR_FILE *
popd

echo "Created distribution at $TAR_FILE"
