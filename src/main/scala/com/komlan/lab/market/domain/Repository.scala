package com.komlan.lab.market.domain

import com.twitter.util.{Future, FuturePool}

import scala.collection.mutable
import com.komlan.lab.market.api._

trait Repository[ID, T <: Id[ID]] {
  def save(entity: T) : Option[T]

  def delete(entity: T): Unit

  def deleteById(id: ID): Unit

  def findById(id: ID): Option[T]

  def findAll(spec: Specification[T] = null): List[T]
}

class InMemoryRepository[ID, T <: Id[ID]] extends Repository[ID, T] {

  val repository: mutable.HashMap[ID, T] = new mutable.HashMap[ID, T]() // In memory

  override def save(entity: T) =
    repository.put(entity.id.get, entity)


  override def delete(entity: T): Unit = repository.remove(entity.id.get)

  override def deleteById(id: ID): Unit = repository.remove(id)

  override def findById(id: ID): Option[T] = repository.get(id)

  override def findAll(spec: Specification[T]): List[T] =
    if (spec == null) repository.values.toList
    else repository.values.filter(x => spec.specified(x)).toList
}

