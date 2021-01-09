package com.komlan.lab.market

import com.google.inject.Module
import com.komlan.lab.market.api._
import com.komlan.lab.market.domain.Repository
import com.komlan.lab.market.modules.DefaultModule
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.{LoadedStatsReceiver, StatsReceiver}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.inject.TwitterModule
import com.twitter.inject.modules.StatsReceiverModule
import com.twitter.util.FuturePools

import scala.reflect.ClassTag


object StockServerMain extends StockServer

//object MyCustomStatsReceiverModule extends TwitterModule {
//
//  override def configure(): Unit = {
//    bind[StatsReceiver].toInstance(LoadedStatsReceiver.scope("oursvr"))
//  }
//}
class StockServer extends HttpServer {

  override val defaultHttpPort:String = ":80"
  override val defaultHttpServerName:String = "StockServer"

  override def modules: Seq[Module] = Seq(
    StatsReceiverModule,
    //DefaultModule
  )

  override protected def start(): Unit = {
  }
  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .add[UserController]
      .add[StockController]
      .add[PortfolioController]
  }

  override protected def warmup(): Unit = {

  }
}
