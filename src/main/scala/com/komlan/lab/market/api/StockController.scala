package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.domain.StockRepository
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class StockController @Inject()(repository: StockRepository) extends Controller {
  val base = "/stocks"

  get(base) { request: Request =>
    repository
      .findAll()
  }

  get(base + "/:symbol") { request: Request =>
    val symbol = request.getParam("symbol")
    repository
      .findById(symbol) match {
      case None => response.notFound(Message(s"Stock with symbol $symbol is not found"))
      case stock => stock

    }
  }

  post(base) { stock: Stock =>

    val toSave = stock.id match {
      case None => stock.copy(id = Option(stock.symbol))
      case _ => stock
    }
    repository
      .save(toSave)

    response
      .created()
      .location(s"$base/${stock.symbol}")

  }
}
