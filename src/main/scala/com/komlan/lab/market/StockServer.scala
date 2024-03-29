package com.komlan.lab.market

import com.google.inject.Module
import com.komlan.lab.market.api.TradeType.{Buy, Sell}
import com.komlan.lab.market.api._
import com.komlan.lab.market.domain.{Repository, StockRepository, TradeRepository, UserRepository}
import com.komlan.lab.market.modules.DefaultModule
import com.komlan.lab.market.services.SetupService
import com.komlan.lab.market.utils.{DateUtils, SQuote}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.{LoadedStatsReceiver, StatsReceiver}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.jackson.ScalaObjectMapper
import com.twitter.inject.TwitterModule
import com.twitter.inject.modules.StatsReceiverModule
import com.twitter.util.{FuturePool, FuturePools}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global


object StockServerMain extends StockServer


class StockServer extends HttpServer {

  override val defaultHttpPort:String = ":80"
  override val defaultHttpServerName:String = "StockServer"

  override def modules: Seq[Module] = Seq(
    StatsReceiverModule
  )

  override protected def configureHttp(router: HttpRouter): Unit = {

    router
      .filter[CommonFilters]
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .add[UserController]
      .add[StockController]
      .add[PortfolioController]
  }

  override protected def setup(): Unit = {

    val setupService = injector.instance[SetupService]
    val setupFuture = setupService.runSetup

    setupFuture onComplete {
      case Success((users, stocks, quotes, trades)) => {
        info(s"Done with setup. Got  ${users.size} users, ${stocks.size} stocks, ${quotes.size} quotes")
      }
      case Failure(error)  =>
        warn(s"Unexpected output from setup. Root cause; ${error.getMessage}")
    }
  }

  override protected def warmup(): Unit = {

  }
}
