package com.komlan.lab.market

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

object FinServerMain extends FinServer

class FinServer extends HttpServer {
  override protected def configureHttp(router: HttpRouter): Unit = ???
}
