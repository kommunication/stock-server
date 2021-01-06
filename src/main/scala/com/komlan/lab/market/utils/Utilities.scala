package com.komlan.lab.market.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.komlan.lab.market.api.StockQuote

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.{Date, Locale}
import scala.concurrent.Future



object DateUtils {
  val formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
  def getDateFromString(str: String):Date = formatter.parse(str)
}


/**
 * IO Utils classes
 */
case class SQuote(date: String, open: Double, high: Double, low: Double, close: Double, volume: Int, @JsonProperty(value="Name") name: String) {
  def toDomainObject(id: Int): StockQuote = StockQuote(id = Some(id), symbol = name, date = DateUtils.getDateFromString(date), openPrice = open, highPrice = high, lowPrice = low, closePrice = close)
}
