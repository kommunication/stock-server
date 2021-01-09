package com.komlan.lab.market.domain.http


import com.komlan.lab.market.api.{Trade, TradeType}
import com.komlan.lab.market.api.TradeType._
import com.twitter.finatra.http.annotations.{QueryParam, RouteParam}
import org.joda.time.DateTime

import java.util.Date

case class TradePostRequest(
               tradeType: String,
               @RouteParam("userId")
               userId:Int,
               symbol: String,
               quantity: Double,
               price: Double,
               date: Option[DateTime]
             ) {
  def toModelObject(id: Int) = {
    val tradeDate = date match {
      case None => new Date(System.currentTimeMillis())
      case Some(dateTime) => dateTime.toDate
    }
    Trade(id = Option(id), tradeType = TradeType.withName(tradeType), userId = userId, symbol= symbol, quantity = quantity, price = price, date = tradeDate, status = "Created" )
  }
}
