package com.komlan.lab.market.services

import com.komlan.lab.market.api.TradeType.{Buy, Sell}
import com.komlan.lab.market.api.{Stock, StockQuote, Trade, User}
import com.komlan.lab.market.domain.{StockQuoteRepository, StockRepository, TradeRepository, UserRepository}
import com.komlan.lab.market.utils.{DateUtils, SQuote}
import com.twitter.finatra.jackson.ScalaObjectMapper
import com.twitter.inject.Logging
import com.twitter.util.FuturePool

import javax.inject.Inject
import scala.util.{Failure, Success, Try}
import com.komlan.lab.market.utils.Implicits._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.concurrent.ExecutionContext.Implicits.global

class SetupService @Inject()(
     userRepo: UserRepository,
     stockRepo: StockRepository,
     tradeRepo: TradeRepository,
     stockQuoteRepository: StockQuoteRepository
     ) extends  Logging {


  /**
   * Run setup to seed in-memory db with test dat
   * Meant to run once, at startup, for demo.
   * @return
   */
  def runSetup() = {

    val user1 = User(1, "komlan", "komlan@gmail.com")
    val user2 = User(2, "bob", "bob@gmail.com")
    val stock1 = Stock(Some("AAPL"), "AAPL", "Apple Inc.")
    val stock2 = Stock(Some("GOOG"), "GOOG", "Google Inc")

    def getUsers():Future[List[User]] = Future {

       List[User](user1, user2)
    }

    def getStocks():Future[List[Stock]] = Future {


      List[Stock](
        stock1,
        stock2
      )
    }

    def getTrades():Future[Seq[Trade]] = Future {
      Seq[Trade](
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
    }

    def getQuotes():Future[List[SQuote]] = Future {
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

      quoteListRaw
    }


    val userFuture = getUsers()
    val stockFuture = getStocks()
    val tradeFuture = getTrades()
    val quoteFuture = getQuotes()

    val result = for {
      users <- userFuture
      stocks <- stockFuture
      trades <- tradeFuture
      quoteListRaw <- quoteFuture
    } yield (users, stocks, quoteListRaw, trades)

   val finalResult = result flatMap { case (users:List[User], stocks: List[Stock], quoteListRaw:List[SQuote], trades:Seq[Trade]) => {
      for (u <- users) {
        userRepo.save(u)
      }
      for (s <- stocks) {
        stockRepo.save(s)
      }
      for (t <- trades) {
        tradeRepo.save(t.copy(id = Option(tradeRepo.getNextId)))
      }


     val quotes = scala.collection.mutable.ListBuffer[StockQuote]()
     val loadedStocks = scala.collection.mutable.ListBuffer[Stock]()
     loadedStocks ++= stocks
     quoteListRaw.foreach(rawQuote => {
        val symbol = rawQuote.name

        val stock = stockRepo.findById(symbol) match {
          case None => {
            debug(s"No existing stock found for $symbol")
            val newStock = Stock(id = Some(symbol), symbol = symbol, name = symbol)

            stockRepo.save(newStock)
            loadedStocks += newStock

            newStock
          }
          case existingStock => existingStock
        }

        val quote = rawQuote.toDomainObject(stockQuoteRepository.getNextId)
        stockQuoteRepository.save(quote)


        quotes += quote

      })


     Future {
       (users, loadedStocks, quotes, trades)
     }

    }}

    finalResult.onComplete {
      case Success((users, stocks, quotes, trades)) =>  {
        (users, stocks, quotes, trades)
      }
      case Failure(e) => warn(e.getMessage)
    }


    finalResult

  }
}
