package com.komlan.lab.market.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.komlan.lab.market.api.StockQuote

import java.text.SimpleDateFormat
import java.time.{LocalDate, ZoneId}
import java.util.{Date, Locale}
import scala.concurrent.Future
import scala.io.Source


object Implicits {
  implicit val formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
}
object DateUtils {
  val formatter_yyyymmdd = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
  val formatter_mmddyy = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH)
  val formatter_mmddyyyy = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)

  val defaultZoneId = ZoneId.systemDefault()


  def getDateFromString(str: String)(implicit formatter: SimpleDateFormat):Date = formatter.parse(str)
  def getDateFromLocalDate(localDate: LocalDate)(implicit timeZoneId: ZoneId = defaultZoneId) = Date.from(localDate.atStartOfDay(timeZoneId).toInstant())
}

object CSV {
  def readCvsFromFile(f: String, skipHeader:Boolean = true): Iterator[Array[String]] =
    processCsvLine(Source.fromFile(f).getLines, skipHeader)

  def readCvsFromResource(f: String, skipHeader:Boolean = true): Iterator[Array[String]] =
    processCsvLine(Source.fromResource(f).getLines, skipHeader)

  private def processCsvLine(src: Iterator[String], skipHeader:Boolean = true):Iterator[Array[String]] = {
    // assuming first line is a header
    val headerLine = if (!skipHeader) src.take(1).next else ""

    // processing remaining lines
    for (l <- src)
      yield l.split(",").map(_.trim) // split line by comma and process them
  }
}

/**
 * IO Utils classes
 */
case class SQuote(date: String, open: Double, high: Double, low: Double, close: Double, volume: Int, @JsonProperty(value="Name") name: String) {
  implicit val formatter = DateUtils.formatter_mmddyy
  def toDomainObject(id: Int): StockQuote = StockQuote(id = Some(id), symbol = name, date = DateUtils.getDateFromString(date), openPrice = open, highPrice = high, lowPrice = low, closePrice = close, volume = volume)
}

class RawTrade(date: String, symbol: String, buyOrSell: String, quantity: Double, sharePrice: Double, totalPrice: Double )
