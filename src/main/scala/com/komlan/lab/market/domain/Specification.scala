package com.komlan.lab.market.domain

import com.komlan.lab.market.api.{Trade, User}

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
}
