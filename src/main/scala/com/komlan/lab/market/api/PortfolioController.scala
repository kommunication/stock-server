package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.domain.http.TradePostRequest
import com.komlan.lab.market.domain.{PortfolioRepository, TradeRepository, UserRepository}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class PortfolioController @Inject()(
                                     portfolioRepo: PortfolioRepository,
                                     userRepo: UserRepository,
                                     tradeRepo: TradeRepository
                                   ) extends Controller {


  // get portfolio
  // /trades -> get all
  // ?symboi=TWITTER => get all trade from twitter
  get("/users/:userId/portfolio") { request: Request =>
    val userId = request.getIntParam("userId")
    userRepo
      .findById(userId)
      .flatMap(user => {
        portfolioRepo.findByUserId(userId)
      }) match {
      case None => response.notFound(Message(s"No portfolio found for user $userId"))
      case portfolio: Portfolio => portfolio
    }


  }

  get("/users/:userId/trades") {request:Request =>
    val userId = request.getIntParam("userId")

    tradeRepo
      .findAll()

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
              .location(s"/users/${entity.userId}/trades/${entity.id}")
          }
        }



  }
}
