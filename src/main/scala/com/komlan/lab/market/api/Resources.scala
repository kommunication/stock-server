package com.komlan.lab.market.api

import com.twitter.finatra.http.annotations.RouteParam

import java.util.Date


trait Entity

trait Id[ID] { entity: Entity =>
  def id:Option[ID]
}

case class Message(message: String)

/**
 * Represent the user of the system
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
