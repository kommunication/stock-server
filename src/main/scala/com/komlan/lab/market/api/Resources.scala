package com.komlan.lab.market.api

import java.util.Date


trait Entity {

}

trait Id[ID] { entity: Entity =>
  def id:Option[ID]
}

case class Message(message: String)


case class User(id: Option[Int], username: String, email: String) extends Entity with Id[Int]
case class Stock(symbol: String, name: String) extends  Entity with Id[String] {
  override def id: Option[String] = Option(symbol)
}
case class StockQuote(
                       id:Option[Int], symbol: String, date:Date, openPrice: Double,
                       highPrice: Double, lowPrice: Double, closePrice: Double
) extends Entity with Id[Int]


case class Trade (
                  id: Option[Int],
                  tradeType: Int, userId:Int, symbol: String,
                  quantity: Double, price: Double, date: Date,
                  status: String
) extends Entity with Id[Int]

// Trade status // for audit


case class Portfolio(id:Option[Int], name:String="", userId: Int, balance: Double, trades: Seq[Trade] ) extends Entity with Id[Int]
