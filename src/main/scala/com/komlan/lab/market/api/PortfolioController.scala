package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.api.TradeType._
import com.komlan.lab.market.domain.http.{PortfolioEvaluateGetRequest, TradePostRequest}
import com.komlan.lab.market.domain.{PortfolioRepository, QuoteSpecification, StockQuoteRepository, StockRepository, TradeRepository, TradeSpecification, UserRepository}
import com.komlan.lab.market.utils.{CSV, DateUtils, SQuote}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.request.RequestUtils
import com.twitter.finatra.jackson.ScalaObjectMapper
import com.twitter.util.FuturePool

import java.io.{File, PrintWriter}
import java.nio.file.Paths
import java.text.SimpleDateFormat
import scala.util.{Failure, Success, Try}

class PortfolioController @Inject()(
 portfolioRepo: PortfolioRepository,
 userRepo: UserRepository,
 tradeRepo: TradeRepository,
 stockRepo: StockRepository,
 stockQuoteRepository: StockQuoteRepository
) extends Controller {

  implicit val formatter = DateUtils.formatter_mmddyyyy

  get("/users/:userId/portfolio/evaluate") { request: PortfolioEvaluateGetRequest =>
    val userId = request.userId
    val dateFrom = request.dateFrom match {
      case Some(fromDate) => fromDate.toDate // DateUtils.getDateFromLocalDate(fromDate)
      case None => DateUtils.getDateFromString("01/01/2017")
    }
    val dateTo = request.dateTo
      .withHourOfDay(23)
      .withMinuteOfHour(59)
      .withSecondOfMinute(59)
      .toDate


    userRepo
      .findById(userId)
    match {
      case None => response.badRequest(Message(s"Unknown user with id $userId"))
      case _ => {

        val trades = tradeRepo
          .findAll(TradeSpecification.ForUser(userId))
          .filter(t => {
            t.date.after(dateFrom) &&
              t.date.before(dateTo)
          })
          .sortBy(trade => trade.date)

        val initialPortfolio = Portfolio(Some(portfolioRepo.getNextId), "", userId, 100000.0, List())
        val portfolioToDate = Portfolio.applyTrades(initialPortfolio, trades)

        val allQuotesForTheDate = stockQuoteRepository.getAllQuotesForDate(dateTo)
          .map(quote => quote.symbol -> quote).toMap

        val stockValuesToDate = portfolioToDate match {
          case None => List()
          case Some(p) => p.stocks.map(stock => {
            val sharePrice = allQuotesForTheDate.get(stock.symbol)
              .headOption match {
              case None => {
                warn(s"Could not find quote for stock ${stock.symbol} for date ${dateTo}")
                0.0
              }
              case Some(quote) => quote.closePrice
            }
            (stock.symbol, dateTo, stock.quantity * sharePrice)
          })
        }
        (portfolioToDate, stockValuesToDate)
      }
    }
  }

  get("/users/:userId/trades") { request: Request =>
    val userId = request.getIntParam("userId")
    val dateFrom = Option(request.getParam("from"))
    val dateTo = Option(request.getParam("to"))

    tradeRepo
      .findAll(TradeSpecification.ForUser(userId))
      .filter(t => {
        (dateFrom.isEmpty || t.date.after(DateUtils.getDateFromString(dateFrom.get))) &&
          (dateTo.isEmpty || t.date.before(DateUtils.getDateFromString(dateTo.get)))
      })
      .sortBy(trade => trade.date)

  }

  post("/users/:userId/trades") { request: TradePostRequest =>
    val userId = request.userId
    userRepo
      .findById(userId)
    match {
      case None => response.badRequest(Message(s"Not a valid user id: $userId"))
      case _ => {
        val entity = request.toModelObject(tradeRepo.getNextId)

        tradeRepo
          .save(entity)

        response
          .created
          .location(s"/users/${entity.userId}/trades/${entity.id.get}")
      }
    }




  }

  post("/users/:userId/trades/upload") { request: Request =>
    val formData = RequestUtils.multiParams(request)
    var content:String = ""
    for ((key, item) <- formData) {
      if (item.isFormField)
        info(s"$key -> ${item.data.toString}")
      else {
        content = (item.data.map(_.toChar)).mkString
        info(s"${key} -> filename: ${item.filename}")
      }

    }
    info(s"Got CSV content: $content")
    val trades = Trade.readFromCsvString(content)


    response.ok().json(
      s"""
        |{
        | "keys": ${formData.keys},
        | "fileContent": ${content},
        | "trades": $trades
        |}
        |""".stripMargin)

  }


  private def storeQuoteData(quotes: List[StockQuote]) = {
    val fmt = new SimpleDateFormat("yyyyMMdd")
    val quoteBySymbol = quotes.groupBy(_.symbol)
      .toList
    for ((symbol, quoteList) <- quoteBySymbol) {
      info(s"Saving data for $symbol, ${quoteList.size} items")
      val dataPath = "data"
      val path = Paths.get(dataPath, s"quotes-by-symbol-${symbol}.csv").toString

      val file = new File(path)
      //file.getParentFile.mkdirs()
      val writer = new PrintWriter(file)

      for (q: StockQuote <- quoteList) {
        val row: List[String] = List(fmt.format(q.date), q.symbol, q.openPrice.toString, q.highPrice.toString, q.lowPrice.toString, q.closePrice.toString, q.volume.toString)
        writer.println(row.mkString(","))
      }
      writer.close()
      info(s"Completed saving data for $symbol")
    }

    val quoteByDate = quotes.groupBy[String](quote => fmt.format(quote.date))
    for ((dateStr, quoteList) <- quoteByDate) {
      val dataPath = "data"
      val path = Paths.get(dataPath, s"quotes-by-date-${dateStr}.csv").toString

      val file = new File(path)
      //file.getParentFile.mkdirs()
      val writer = new PrintWriter(file)

      for (q: StockQuote <- quoteList) {
        val row: List[String] = List(fmt.format(q.date), q.symbol, q.openPrice.toString, q.highPrice.toString, q.lowPrice.toString, q.closePrice.toString, q.volume.toString)
        writer.println(row.mkString(","))
      }
      writer.
        close()
    }
  }
}



