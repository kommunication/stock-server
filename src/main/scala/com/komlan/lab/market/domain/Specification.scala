package com.komlan.lab.market.domain

import com.komlan.lab.market.api.{StockQuote, Trade, User}
import com.komlan.lab.market.utils.DateUtils

import java.util.Date

trait Specification[T] {
  def specified(entity: T): Boolean

  def toSqlClauses(): String
}


class PredicateSpec[T](p: T => Boolean) extends Specification[T] {
  override def specified(entity: T) :Boolean = p(entity)

  override def toSqlClauses(): String = ""
}

trait TradeSpecification extends Specification[Trade]
object TradeSpecification {
  case class ForUser(userId: Int) extends TradeSpecification {

    override def specified(entity: Trade): Boolean = entity.userId == userId

    override def toSqlClauses(): String = s"userId = $userId" // leaving this out of the scope
  }
  case class ForUserWithDateRange(start:Date, end: Date) extends TradeSpecification {
    override def specified(entity: Trade): Boolean = entity.date.after(start) && entity.date.before(end)

    override def toSqlClauses(): String = "" // To implement
  }
}

trait QuoteSpecification extends Specification[StockQuote]

object QuoteSpecification {
  import com.komlan.lab.market.utils.Implicits._
  case class ForStockAndDate(symbol: String, date: Date) extends QuoteSpecification {
    override def specified(entity: StockQuote): Boolean = entity.symbol == symbol &&
      formatter.format(entity.date).equals(formatter.format(date))

    override def toSqlClauses(): String = "" // Not implemented
  }
}
