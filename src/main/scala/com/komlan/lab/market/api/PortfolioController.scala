package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.api.TradeType._
import com.komlan.lab.market.domain.http.TradePostRequest
import com.komlan.lab.market.domain.{PortfolioRepository, StockQuoteRepository, StockRepository, TradeRepository, TradeSpecification, UserRepository}
import com.komlan.lab.market.utils.{DateUtils, SQuote}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.jackson.ScalaObjectMapper
import com.twitter.util.FuturePool

import java.sql.Date
import scala.reflect.io.File
import scala.util.{Failure, Success, Try}

class PortfolioController @Inject()(
 portfolioRepo: PortfolioRepository,
 userRepo: UserRepository,
 tradeRepo: TradeRepository,
 stockRepo: StockRepository,
 stockQuoteRepository: StockQuoteRepository

) extends Controller {


  get("/users/:userId/portfolio") { request: Request =>
    val userId = request.getIntParam("userId")
    userRepo
      .findById(userId)
      match {
      case None => response.badRequest(Message(s"Unknown user with id $userId"))
      case _ => {
        tradeRepo.findAll(TradeSpecification.ForUser(userId))
      }
    }
  }

  get("/users/:userId/trades") {request:Request =>
    val userId = request.getIntParam("userId")

    tradeRepo
      .findAll(TradeSpecification.ForUser(userId))

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

  get("/setup"){
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

      val trades = Seq[Trade](
        Trade(None, Buy, user1.id.get, stock1.symbol, 2.5, 1050.12, DateUtils.getDateFromString("2017/10/01"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
        Trade(None, Sell, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
        Trade(None, Buy, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
        Trade(None, Sell, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/06"), status = "Created"),
        Trade(None, Buy, user2.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
        Trade(None, Sell, user2.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
        Trade(None, Buy, user2.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
      )

      for (t <- trades) {
        tradeRepo.save(t.copy(id = Option(tradeRepo.getNextId)))
      }

      val ressource = getClass().getClassLoader().getResourceAsStream("top100.json")
      val mapper = ScalaObjectMapper()
      info(s"Start reading file")
      val quoteListRaw = Try(mapper.parse[List[SQuote]](ressource)) match {
        case Failure(e) => {
          error(s"Error while parsing resource file. Reason: ${e.getMessage} ")
          List()
        }
        case Success(value) => value
      }
      info(s"Done reading file. Got ${quoteListRaw.size} raw quotes")
      ressource.close()

      val quotes = quoteListRaw.map( rawQuote => {
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
        //stockQuoteRepository.save(quote)
        quote
      })
      (users, stocks, quotes)
    }

    request:Request =>{
      val result = FuturePool.unboundedPool { runSetup }
      result map {
        case (users, stocks, quotes) =>
          response
            .ok
            .json(
              s"""
                |{
                |"counts": {
                | "users": ${users.size},
                | "stocks": ${stocks.size},
                | "quotes": ${quotes.size}
                | }
                |}
                |""".stripMargin)
      }
    }


  }



}
