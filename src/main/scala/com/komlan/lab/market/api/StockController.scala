package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.domain.http.StockQuoteGetRequest
import com.komlan.lab.market.domain.{StockQuoteRepository, StockRepository}
import com.komlan.lab.market.utils.DateUtils
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.FuturePool

class StockController @Inject()(repository: StockRepository, stockQuoteRepository: StockQuoteRepository) extends Controller {
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

  get(base + "/:symbol/quotes") { request: StockQuoteGetRequest =>
    val symbol = request.symbol
    val dateFrom = request.dateFrom
    val dateTo = request.dateTo

    FuturePool.unboundedPool {
      stockQuoteRepository.getAllQuotesForSymbol(symbol, dateFrom, dateTo)
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
