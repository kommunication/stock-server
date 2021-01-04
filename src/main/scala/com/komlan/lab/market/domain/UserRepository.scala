package com.komlan.lab.market.domain

import com.google.inject.{Inject, Singleton}
import com.komlan.lab.market.api._
import com.twitter.util.{Future, FuturePool}

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


