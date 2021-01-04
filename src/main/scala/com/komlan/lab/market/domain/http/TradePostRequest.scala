package com.komlan.lab.market.domain.http

import com.komlan.lab.market.api.Trade
import com.twitter.finatra.http.annotations.RouteParam

import java.util.Date

case class TradePostRequest(
               tradeType: Int,
               @RouteParam("userId")
               userId:Int,
               symbol: String,
               quantity: Double,
               price: Double
//               ,
//               date: Date
             ) {
  def toModelObject(id: Int) = {
    Trade(id = Option(id), tradeType = tradeType, userId = userId, symbol= symbol, quantity = quantity, price = price, date = new Date(System.currentTimeMillis()), status = "Created" )
  }
}
