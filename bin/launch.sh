#!/bin/sh

command="launch"

if [ ! -z "$1" ]
  then
    command=$1
fi

echo "input is $command "

/sar/ec2launch/spark-ec2-spot -k sar_aws_key -i /sar/aws/sar_aws_key.pem --spark-ec2-git-repo=https://github.com/florianverhein/spark-ec2 --spark-ec2-git-branch=packer -a ami-ed323add -r us-west-2 -z us-west-2c -t m3.medium -s 1 --spot-price=0.03 $command sar_cl1