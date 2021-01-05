package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.domain.http.TradePostRequest
import com.komlan.lab.market.domain.{PortfolioRepository, StockRepository, TradeRepository, TradeSpecification, UserRepository}
import com.komlan.lab.market.utils.DateUtils
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

import java.sql.Date

class PortfolioController @Inject()(
                                     portfolioRepo: PortfolioRepository,
                                     userRepo: UserRepository,
                                     tradeRepo: TradeRepository,
                                     stockRepo: StockRepository
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

  get("/setup"){request:Request =>
    val user1 = User(id=Option(1),username = "komlan", email = "komlan@gmail.com" )
    val user2 = User(id=Option(2), username = "bob", email = "bob@gmail.com")
    val users = List[User](user1, user2)
    for (u <-  users){
      userRepo.save(u)
    }

    val stock1 = Stock(Some("AAPL"), "AAPL", "Apple Inc.")
    val stock2 = Stock(Some("GOOG"), "GOOG", "Google Inc")
    val stocks = List[Stock](
      stock1,
      stock2
    )
    for (s <- stocks){
      stockRepo.save(s)
    }

    val trades = Seq[Trade](
      Trade(None, 1, user1.id.get, stock1.symbol, 2.5, 1050.12, DateUtils.getDateFromString("2017/10/01"), status = "Created"),
      Trade(None, 1, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
      Trade(None, -1, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
      Trade(None, 1, user1.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
      Trade(None, 1, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
      Trade(None, 1, user1.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/05"), status = "Created"),
      Trade(None, -1, user1.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/06"), status = "Created"),
      Trade(None, 1, user2.id.get, stock2.symbol, 1.5, 1050.12, DateUtils.getDateFromString("2017/10/02"), status = "Created"),
      Trade(None, -1, user2.id.get, stock1.symbol, 2.0, 925.12, DateUtils.getDateFromString("2017/10/03"), status = "Created"),
      Trade(None, 1, user2.id.get, stock2.symbol, 2.5, 950.12, DateUtils.getDateFromString("2017/10/04"), status = "Created"),
    )

    for (t <- trades){
      tradeRepo.save(t.copy(id = Option(tradeRepo.getNextId)))
    }


  }



}
