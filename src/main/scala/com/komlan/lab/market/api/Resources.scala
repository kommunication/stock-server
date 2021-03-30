package com.komlan.lab.market.api

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.komlan.lab.market.api.TradeType.{Buy, Sell, TradeType}
import com.komlan.lab.market.utils.{CSV, DateUtils}
import com.twitter.finatra.http.annotations.RouteParam
import com.twitter.finatra.jackson.ScalaObjectMapper
import com.twitter.inject.Logging

import java.text.SimpleDateFormat
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
                       highPrice: Double, lowPrice: Double, closePrice: Double, volume: Long
) extends Entity with Id[Int]

object StockQuote {
  def readFromCsv(filename: String, skipHeader:Boolean = true): List[StockQuote] = {
    implicit val formatter = new SimpleDateFormat("yyyyMMdd")
    //20170202,AAPL,127.975,129.39,127.78,128.53
    CSV.readCvsFromFile(filename, skipHeader)
      .filter(row => row.nonEmpty && row.size >= 7)
      .map(row =>
        StockQuote(
          date = DateUtils.getDateFromString(row(0)),
          symbol = row(1),
          openPrice = row(2).toDouble,
          highPrice = row(3).toDouble,
          lowPrice = row(4).toDouble,
          closePrice = row(5).toDouble,
          volume = row(6).toLong,
          id = None
        )
      ).toList
  }
}

object TradeType extends Enumeration {
  type TradeType = Value
  val Buy = Value("buy")
  val Sell = Value("sell")
}

class TradeTypeType extends TypeReference[TradeType.type]
case class TradeTypeHolder(@JsonScalaEnumeration(classOf[TradeTypeType]) tradeType: TradeType.TradeType)


case class Trade (
                   id: Option[Int]=None,
                   @JsonScalaEnumeration(classOf[TradeTypeType])
                   tradeType: TradeType,
                   userId:Int,
                   symbol: String,
                   quantity: Double,
                   price: Double,
                   date: Date,
                   status: String = "Created"
) extends Entity with Id[Int]

object Trade {
  def readFromCsv(filename: String): List[Trade] = {
    implicit val formatter = DateUtils.formatter_mmddyyyy
    readFromCsvText(CSV.readCvsFromFile(filename))

  }
  def readFromCsvString(text: String): List[Trade] = {
    implicit val formatter = DateUtils.formatter_mmddyyyy
    readFromCsvText(CSV.processCsvLines(text.lines))
  }

  private def readFromCsvText(lines: Iterator[Array[String]]) : List[Trade] = {
    implicit val formatter = DateUtils.formatter_mmddyyyy
    lines
      .filter(row => row.nonEmpty && row.size >= 6)
      .map(row =>
        Trade(
          date = DateUtils.getDateFromString(row(0)),
          symbol = row(1),
          tradeType = TradeType.withName(row(2)),
          quantity = row(3).toDouble,
          price = row(5).toDouble,
          userId = -1
        )
      ).toList
  }
}


case class StockPosition(userId: Int, portfolioId: Int, symbol: String, quantity: Double)


/**
 * Represents a User's porfolio, a list of stock positions
 * @param id
 * @param name
 * @param userId
 * @param balance
 * @param stocks
 */
case class Portfolio (
         id:Option[Int],
         name: String="",
         userId: Int,
         balance: Double, // Cash balance (initial or current)
         stocks: List[StockPosition]
) extends Entity with Id[Int] {

  def getStockPosition(symbol: String): Option[StockPosition] = stocks.find((p: StockPosition) => p.symbol == symbol)
}


object Portfolio extends Logging {


  /**
   * Given a portfolio and a list of incoming trades, applies the trades subject to
   * available budget (portfolio.balance) and return a copy of the portfolio with
   * updated balance and list of new stock positions.
   *
   * @param portfolio
   * @param newTrades
   * @return
   */
  def applyTrades(portfolio:Portfolio, newTrades: Seq[Trade]): Option[Portfolio] = {
    val currentPositions = portfolio.stocks
    val additionPositions = newTrades
      .groupBy(t => t.symbol)
      .map({
        case (symbol, trades) => {
          val quantity = trades.foldLeft(0.0)((s, t) => s + (if (t.tradeType == Buy) t.quantity else -t.quantity))
          val cost = trades.foldLeft(0.0)((s, t) => s + (if (t.tradeType == Buy) -t.price else t.price))
          (symbol, quantity, cost)
        }
      })

    val newBalance = additionPositions.foldLeft(portfolio.balance)((s, v) => s + v._3)

    if (newBalance < 0.0) {
      warn(s"Unable to apply new trades to portfolio (${portfolio.id}). Reason: new balance negative ($newBalance)")
      None  // Can't apply trades that overdraw budget
    } else {
      info(s"Applying trades to portfolio (${portfolio.id}). New balance ($newBalance)")
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


  def loadStockQuotation(jsonFile: String): Unit ={
    val mapper = ScalaObjectMapper()
    val jsonStr =
      """
        |{
        | "username": "komlan",
        | "email":"komlan@gmail.com"
        |}
        |""".stripMargin

    val json = mapper.parse[User](jsonStr)
  }
}
