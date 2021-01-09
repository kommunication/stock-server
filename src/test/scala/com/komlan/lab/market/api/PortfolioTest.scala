package com.komlan.lab.market.api


import com.fasterxml.jackson.dataformat.csv.{CsvMapper, CsvSchema}
import com.komlan.lab.market.api.TradeType._
import com.komlan.lab.market.utils.{CSV, DateUtils, RawTrade, SQuote}
import com.twitter.finatra.jackson.ScalaObjectMapper
import org.scalatest.{BeforeAndAfter, FunSuite}

import java.io.ByteArrayInputStream
import java.util.Date
import java.time.LocalDate
import scala.jdk.CollectionConverters.asScalaIteratorConverter

class PortfolioTest extends FunSuite with BeforeAndAfter {

  test("apply empty list of trades to portfolio"){
    val portfolio = Portfolio(id=Some(1), userId = 1, balance = 0.0, stocks = List() )
    val maybePortfolio = Portfolio.applyTrades(portfolio, List())
    assert(maybePortfolio.nonEmpty)
    val newPortfolio = maybePortfolio.get
    assert(newPortfolio == portfolio)
  }

  test("apply new trade to empty portfolio"){
    val portfolio = Portfolio(id=Some(1), userId = 1, balance = 1000.0, stocks = List() )

    val buyGoogle1 = Trade(symbol = "GOOG", tradeType = Buy, quantity = 1.0, price = 10.0, userId = 1, status = "Created", date = new Date(System.currentTimeMillis()))

    val maybePortfolio = Portfolio.applyTrades(portfolio, List(buyGoogle1))
    assert(maybePortfolio.nonEmpty)
    val newPortfolio = maybePortfolio.get
    // The new portfolio should have a stock position for GOOG
    assert(newPortfolio.getStockPosition("GOOG").nonEmpty)
  }

  test("apply 'buy' trade to existing portfolio"){
    val portfolio = Portfolio(id=Some(1), userId = 1, balance = 1000.0, stocks = List(
      StockPosition(symbol = "AAPL", quantity = 1.0, userId = 1, portfolioId = 1),
      StockPosition(symbol = "GOOG", quantity = 2.326, userId = 1, portfolioId = 1)
    ))
    val trade = Trade(symbol = "GOOG", tradeType = Buy, quantity = 1.0, price = 10.0, userId = 1, status = "Created", date = new Date(System.currentTimeMillis()))
    val newTrades = List(trade)
    val maybePortfolio = Portfolio.applyTrades(portfolio, newTrades)
    assert(maybePortfolio.nonEmpty)

    val newPortfolio = maybePortfolio.get
    assert(newPortfolio.id == portfolio.id)
    assert(newPortfolio.balance == 990.0 ) // 1000.0 - 10.0 = 990.0

    val googStock = newPortfolio.getStockPosition("GOOG")
    assert(googStock.nonEmpty)
    assert(googStock.get.quantity == 3.326 ) // 2.326 + 1.0 = 3.326
  }

  test("apply 'buy' and 'sell' trades to existing portfolio"){
    val portfolio = Portfolio(id=Some(1), userId = 1, balance = 1000.0, stocks = List(
      StockPosition(symbol = "AAPL", quantity = 1.0, userId = 1, portfolioId = 1),
      StockPosition(symbol = "GOOG", quantity = 2.326, userId = 1, portfolioId = 1)
    ))

    // buy 1 share, then buy 2 shares, then sell 3 share
    val buyGoogle1 = Trade(symbol = "GOOG", tradeType = Buy, quantity = 1.0, price = 10.0, userId = 1, status = "Created", date = new Date(System.currentTimeMillis()))
    val buyGoogle2 = Trade(symbol = "GOOG", tradeType = Buy, quantity = 2.0, price = 20.0, userId = 1, status = "Created", date = new Date(System.currentTimeMillis()))
    val sellGoogle1 = Trade(symbol = "GOOG", tradeType = Sell, quantity = 3.0, price = 30.0, userId = 1, status = "Created", date = new Date(System.currentTimeMillis()))
    val newTrades = List(buyGoogle1, buyGoogle2, sellGoogle1)
    val maybePortfolio = Portfolio.applyTrades(portfolio, newTrades)
    assert(maybePortfolio.nonEmpty)

    val newPortfolio = maybePortfolio.get
    assert(newPortfolio.id == portfolio.id)
    assert(newPortfolio.balance == 1000.0 ) // 1000.0 - 10.0 - 20.0 + 30.0 = 1000.0

    val googStock = newPortfolio.getStockPosition("GOOG")
    assert(googStock.nonEmpty)
    assert(googStock.get.quantity == 2.326 ) // 2.326 + 1.0 + 2.0 -3.0 = 2.326
  }

  test("read Json from string"){
    val mapper = ScalaObjectMapper()
    val jsonStr =
      """
        |{
        | "username": "komlan",
        | "email":"komlan@gmail.com"
        |}
        |""".stripMargin

    val user = mapper.parse[User](jsonStr)
    assert(user.id.isEmpty)
    assert(user.username == "komlan")

    val userListStr =
      """
        |[
        |{
        | "username": "komlan",
        | "email":"komlan@gmail.com"
        |},
        |{
        | "username": "bob",
        | "email":"bob@gmail.com"
        |}
        |]
        |""".stripMargin

        val users = mapper.parse[List[User]](userListStr)
        assert(users.size == 2)
        assert(users.head.username == "komlan")
  }

  test("read quotation data from json string"){

    val jsonStr =
      """
        |{"date":"8/21/17","open":"157.5","high":"157.89","low":"155.1101","close":"157.21","volume":"26368528","Name":"AAPL"}
        |""".stripMargin

    val mapper = ScalaObjectMapper()
    val quote = mapper.parse[SQuote](jsonStr)
    assert(quote != null)
    assert(quote.name == "AAPL")

    val listOfQuoteStr =
      """
        |[
        |{"date":"8/17/17","open":"160.52","high":"160.71","low":"157.84","close":"157.86","volume":"27940565","Name":"AAPL"},
        |{"date":"8/18/17","open":"157.86","high":"159.5","low":"156.72","close":"157.5","volume":"27428069","Name":"AAPL"},
        |{"date":"8/21/17","open":"157.5","high":"157.89","low":"155.1101","close":"157.21","volume":"26368528","Name":"AAPL"},
        |{"date":"8/22/17","open":"158.23","high":"160","low":"158.02","close":"159.78","volume":"21604585","Name":"AAPL"},
        |{"date":"8/23/17","open":"159.07","high":"160.47","low":"158.88","close":"159.98","volume":"19399081","Name":"AAPL"}
        |]
        |""".stripMargin

    val quotes = mapper.parse[List[SQuote]](listOfQuoteStr)
    assert(quotes != null)
    assert(quotes.size == 5)
    assert(quotes.head.open == 160.52)

  }

  test("read trade data csv "){
    implicit val formatter = DateUtils.formatter_mmddyyyy
    /**
     *  From test/resources/trades.csv
     *  Date->0,Symbol->1,BuyOrSell->2,Quantity->3,sharePrice->4,totalPrice->5
     */
    val trades = CSV.readCvsFromResource("trades.csv")
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

    assert(trades.nonEmpty)
    assert(trades.size == 2)
    assert(trades.head.symbol == "GOOGL")
    assert(trades.head.quantity == 10.0)
    val trades2 = trades.tail
    assert(trades2.head.quantity == 5.0)
  }

  test("read trade data csv from file "){
    implicit val formatter = DateUtils.formatter_mmddyyyy
    /**
     *  Note --> copy to external and change path to valid external csv file
     *
     *  Date,Symbol,BuyOrSell,Quantity,sharePrice,totalPrice
     *  01/10/2017,AAPL,buy,10,1722.5,17225.0
     *  01/12/2017,GOOGL,buy,5,1702.3,17023.0
     *  01/13/2017,AAPL,buy,100,702.3,7023.0
     *
     *  Date->0,Symbol->1,BuyOrSell->2,Quantity->3,sharePrice->4,totalPrice->5
     */



    val trades = CSV.readCvsFromFile("c:\\projects\\trades_external.csv")
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

    assert(trades.nonEmpty)
    assert(trades.size == 3)
    val firstTrade = trades.head
    assert(firstTrade.quantity == 10.0)
    assert(firstTrade.symbol == "AAPL")
    val secondTrade:Trade = trades.tail.head
    assert(secondTrade.quantity == 5.0)
  }

}
