package com.komlan.lab.market.domain

trait Specification[T] {
  def specified(entity: T): Boolean

  def toSqlClauses(): String
}
