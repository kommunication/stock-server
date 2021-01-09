package com.komlan.lab.market.domain

import com.google.inject.{Inject, Singleton}
import com.komlan.lab.market.api._
import com.twitter.util.{Future, FuturePool}

import java.nio.file.Paths
import java.util.Date
import java.text.SimpleDateFormat
import scala.collection.mutable

trait AutoIncrementCounter {
  import java.util.concurrent.atomic.AtomicInteger
  val idCounter = new AtomicInteger(0)

  def getNextId = idCounter.incrementAndGet()
}


@Singleton
class UserRepository extends InMemoryRepository[Int, User]{
  import java.util.concurrent.atomic.AtomicInteger
  val idCounter = new AtomicInteger(0)

  def getNextId = idCounter.incrementAndGet()

}

@Singleton
class StockRepository extends InMemoryRepository[String, Stock]  with AutoIncrementCounter {

}

@Singleton
class TradeRepository extends InMemoryRepository[Int, Trade] with AutoIncrementCounter

@Singleton
class PortfolioRepository
  extends InMemoryRepository[Int, Portfolio]
    with AutoIncrementCounter {

  def findByUserId(userId: Int) = {
    repository.get(userId)
  }
}

@Singleton
class StockQuoteRepository
  extends InMemoryRepository[Int, StockQuote]
    with AutoIncrementCounter
  {
    val fmt = new SimpleDateFormat("yyyMMdd")
    def getDataFileForDate( date: Date) = {
      val dataPath = "data"
      Paths.get(dataPath, s"quotes-by-date-${fmt.format(date)}.csv").toString
    }
    def getDataFileForSymbol( symbol: String) = {
      val dataPath = "data"
      Paths.get(dataPath, s"quotes-by-symbol-${symbol}.csv").toString
    }

    def getAllQuotesForDate(date: Date):List[StockQuote] = StockQuote.readFromCsv(getDataFileForDate(date), false)

    def getAllQuotesForSymbol(symbol: String): List[StockQuote] = StockQuote.readFromCsv(getDataFileForSymbol(symbol), false)
}

