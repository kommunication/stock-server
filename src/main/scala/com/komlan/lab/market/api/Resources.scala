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
case class Trade(symbol: String, amount: Double, date: Date)


case class Portfolio(id:Option[Int], name:String="", userId: Int, cashBalance: Double, trades: List[Trade] ) extends Entity with Id[Int]
