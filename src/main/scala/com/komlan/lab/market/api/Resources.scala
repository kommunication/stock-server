package com.komlan.lab.market.api

//object Resources {
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

//}
