package com.sar.spark.server

import akka.actor.{ ActorSystem, ActorRef }
import com.typesafe.config.{ Config, ConfigFactory, ConfigException, ConfigRenderOptions }
import spark.jobserver.WebApi
import spray.routing.Route
import spark.jobserver.util.CORSDirectives

class DQWebApi(system: ActorSystem,
               config: Config,
               port: Int,
               jarManager: ActorRef,
               supervisor: ActorRef,
               jobInfo: ActorRef) extends WebApi(system: ActorSystem,
  config: Config,
  port: Int,
  jarManager: ActorRef,
  supervisor: ActorRef,
  jobInfo: ActorRef) with CORSDirectives {

  override def start() {
    println("Override in DQWebAPI")

    super.start()
  }

  override def otherRoutes: Route =
    corsFilter(List("*")) {
      super.otherRoutes ~ pathPrefix("dqapi") {
        get {
          { ctx =>
            logger.info("Receiving healthz check request")
            ctx.complete("OK")
          }
        }
      }
    }

  override def jobRoutes: Route = corsFilter(List("*")) { super.jobRoutes }

}

