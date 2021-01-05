package com.komlan.lab.market.api

import com.twitter.finatra.http.annotations.RouteParam

import java.time.LocalDate
import java.util.Date


trait Entity

trait Id[ID] { entity: Entity =>
  def id:Option[ID]
}

case class Message(message: String)

/**
 * Represents the user of the system
 * @param id
 * @param username
 * @param email
 */
case class User(id: Option[Int], username: String, email: String) extends Entity with Id[Int]
case class Stock(id:Option[String], symbol: String, name: String) extends  Entity with Id[String]
case class StockQuote( id:Option[Int], symbol: String, date:Date, openPrice: Double,
                       highPrice: Double, lowPrice: Double, closePrice: Double
) extends Entity with Id[Int]

case class Trade (
                   id: Option[Int]=None,
                   tradeType: Int,
                   userId:Int,
                   symbol: String,
                   quantity: Double,
                   price: Double,
                   date: Date,
                   status: String
) extends Entity with Id[Int]

// Trade status // for audit

case class StockPosition(userId: Int, portfolioId: Int, symbol: String, quantity: Double)
case class Portfolio (
         id:Option[Int],
         name: String="",
         userId: Int,
         balance: Double, // Cash balance (initial or current)
         stocks: List[StockPosition]
) extends Entity with Id[Int]


object Portfolio {


  /**
   * Given a portfolio and a list of incoming trades, applies the trades subject to
   * available budget (portfolio.balance) and return a copy of the portfolio with
   * updated balance and list of new stock positions.
   *
   * @param portfolio
   * @param allTrades
   * @return
   */
  def applyTrades(portfolio:Portfolio, allTrades: Seq[Trade]): Option[Portfolio] = {
    val currentPositions = portfolio.stocks
    val additionPositions = allTrades
      .groupBy(t => t.symbol)
      .map({
        case (symbol, trades) => {
          val quantity = trades.foldLeft(0.0)((s, t) => s + (if (t.tradeType == "1") +t.quantity else -t.quantity))
          val cost = trades.foldLeft(0.0)((s, t) => s + (if (t.tradeType == "1") +t.price else -t.price))
          (symbol, quantity, cost)
        }
      })

    val newBalance = additionPositions.foldLeft(portfolio.balance)((s, v) => s + v._3)

    if (newBalance < 0.0) {
      None  // Can't apply trades that overdraw budget
    } else {

      val newPositions:List[StockPosition] = additionPositions.map({
        case (symbol, quantityToAdd, _) => {
          val position = currentPositions
            .find(p => p.symbol ==  symbol)
            .getOrElse(StockPosition(userId=portfolio.userId, portfolioId=portfolio.id.get, symbol=symbol, quantity=0.0))
          position.copy(quantity = position.quantity + quantityToAdd)
        }
      }).toList


      Some(portfolio.copy(balance = newBalance, stocks = newPositions))
    }

  }
}
