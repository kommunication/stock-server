package com.komlan.lab.market

import com.google.inject.Module
import com.komlan.lab.market.api._
import com.komlan.lab.market.domain.Repository
import com.komlan.lab.market.modules.DefaultModule
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.{LoadedStatsReceiver, StatsReceiver}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.inject.TwitterModule
import com.twitter.inject.modules.StatsReceiverModule
import com.twitter.util.FuturePools

import scala.reflect.ClassTag


object StockServerMain extends StockServer

object MyCustomStatsReceiverModule extends TwitterModule {

  override def configure(): Unit = {
    bind[StatsReceiver].toInstance(LoadedStatsReceiver.scope("oursvr"))
  }
}
class StockServer extends HttpServer {

  override val defaultHttpPort:String = ":8080"
  override def modules: Seq[Module] = Seq(
    StatsReceiverModule,
    DefaultModule
  )

  override protected def start(): Unit = {
  }
  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .add[UserController]
      .add[StockController]
      .add[PortfolioController]
  }

  override protected def warmup(): Unit = {

  }
}

abstract class CrudController[ID, T <: Id[ID]] extends Controller {
  protected val repository: Repository[ID, T]
  private val futurePool = FuturePools.unboundedPool
  def base:String

  def getTag()(implicit tag: ClassTag[T]) = tag.getClass


  get(base) { request:Request =>

    futurePool {

      val result = repository
        .findAll()
        .map({
          case element: T => Some(element)
          case _ => None
        })

      response.ok(result)

    }
  }

  /**
   * Get specific user
   */
  get(base + "/:id") { request: Request =>
    repository
      .findById(request.getParam("id").asInstanceOf[ID]) match {
      case None => response.notFound(Message(s"User with id ${request.getIntParam("id")} not found"))
      case Some(user) => user
    }
  }

  delete(base + "/:id"){ request:Request =>
    repository
      .deleteById(request.getParam("id").asInstanceOf[ID])
  }
//
//  /**
//   *  Create new user
//   */
//  post(base) { user: T =>
//
////    val toSave = user.id match {
////      case None => user.copy(id=Option(repository.getNextId))
////      case _ => user
////    }
//    val toSave = user
//
//    repository
//      .save(toSave)
//
//    response
//      .created
//      .location(s"$base/${toSave.id.get}")
//
//  }
}








//class com.komlan.lab.market.api.StockController @Inject() (repo: StockRepository) extends com.komlan.lab.market.CrudController[String, Stock] {
//  override def base = "/stocks"
//
//  override protected val repository: Repository[String, Stock] = repo
//}