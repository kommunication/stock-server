package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.api.TradeType._
import com.komlan.lab.market.domain.http.{PortfolioEvaluateGetRequest, TradePostRequest}
import com.komlan.lab.market.domain.{PortfolioRepository, QuoteSpecification, StockQuoteRepository, StockRepository, TradeRepository, TradeSpecification, UserRepository}
import com.komlan.lab.market.utils.{DateUtils, SQuote}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
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
    val dateTo = request.dateTo.toDate // DateUtils.getDateFromLocalDate(request.dateTo)


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


  /**
   *
   * For endpoint testing only
   */

  get("/setup") {

    def runSetup = {
      val user1 = User(id = Option(1), username = "komlan", email = "komlan@gmail.com")
      val user2 = User(id = Option(2), username = "bob", email = "bob@gmail.com")
      val users = List[User](user1, user2)
      for (u <- users) {
        userRepo.save(u)
      }

      val stock1 = Stock(Some("AAPL"), "AAPL", "Apple Inc.")
      val stock2 = Stock(Some("GOOG"), "GOOG", "Google Inc")
      val stocks = List[Stock](
        stock1,
        stock2
      )
      for (s <- stocks) {
        stockRepo.save(s)
      }
      implicit val formatter = DateUtils.formatter_yyyymmdd
      val trades = Seq[Trade](
        Trade(None, Buy, user1.id.get, stock1.symbol, 2.5, 1050.12, DateUtils.getDateFromString("2017/10/01"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
        Trade(None, Sell, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
        Trade(None, Sell, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/06"), status = "Created"),
        Trade(None, Buy, user2.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
        Trade(None, Buy, user2.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
        Trade(None, Buy, user2.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
      )

      for (t <- trades) {
        tradeRepo.save(t.copy(id = Option(tradeRepo.getNextId)))
      }

      val resource = getClass().getClassLoader().getResourceAsStream("top100.json")
      val mapper = ScalaObjectMapper()
      info(s"Start reading file")
      val quoteListRaw = Try(mapper.parse[List[SQuote]](resource)) match {
        case Failure(e) => {
          error(s"Error while parsing resource file. Reason: ${e.getMessage} ")
          List()
        }
        case Success(value) => value
      }
      info(s"Done reading file. Got ${quoteListRaw.size} raw quotes")
      resource.close()

      val quotes = quoteListRaw.map(rawQuote => {
        val symbol = rawQuote.name
        val stock = stockRepo.findById(symbol) match {
          case None => {
            val newStock = Stock(id = Some(symbol), symbol = symbol, name = symbol)
            stockRepo.save(newStock)
            newStock
          }
          case existingStock => existingStock
        }

        val quote = rawQuote.toDomainObject(stockQuoteRepository.getNextId)
        stockQuoteRepository.save(quote)
        quote
      })

      //storeQuoteData(quotes)

      (users, stocks, quotes)
    }

    request: Request => {
      val result = FuturePool.unboundedPool {
        runSetup
      }
      result map {
        case (users, stocks, quotes) =>
          response
            .ok
            .json(
              s"""
                |{
                | "message": "Test data loaded",
                | "counts": {
                | "users": ${users.size},
                | "stocks": ${stocks.size},
                | "quotes": ${quotes.size}
                | }
                |}
                |""".stripMargin)
      }
    }
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



