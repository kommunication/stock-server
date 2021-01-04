import com.google.inject.Inject
import com.komlan.lab.market.api._
import com.komlan.lab.market.domain.{Repository, StockRepository, UserRepository}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.{Future, FuturePools}
import scala.reflect.api._

import scala.reflect.ClassTag

//import javax.inject.Inject

object StockServerMain extends StockServer


class StockServer extends HttpServer {

  override val defaultHttpPort:String = ":8080"
  override protected def configureHttp(router: HttpRouter): Unit =
    router
      .add[UserController]
      .add[StockController]
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

class UserController @Inject()(repository: UserRepository) extends Controller {


  /**
   * Get all users
   */
  protected val base = "/users"
  get(base) { request:Request =>
    repository
      .findAll()
  }

  /**
   * Get specific user
   */
  get(base + "/:id") { request: Request =>
    repository
      .findById(request.getIntParam("id")) match {
      case None => response.notFound(Message(s"User with id ${request.getIntParam("id")} not found"))
      case Some(user) => user
    }
  }

  delete(base + "/:id"){ request:Request =>
    repository
      .deleteById(request.getIntParam("id"))
  }

  /**
   *  Create new user
   */
  post(base) { user: User =>

    val toSave = user.id match {
      case None => user.copy(id=Option(repository.getNextId))
      case _ => user
    }

    repository
        .save(toSave)

      response
        .created
        .location(s"$base/${toSave.id.get}")

  }

}


class StockController @Inject() (repository: StockRepository) extends Controller {
  val base = "/stocks"

  get(base) {request: Request =>
    repository
      .findAll()
  }

  get(base + "/:symbol"){ request: Request =>
    val symbol = request.getParam("symbol")
    repository
      .findById(symbol) match {
        case None => response.notFound(Message(s"Stock with symbol $symbol is not found"))
        case stock => stock

      }
  }

  post(base) { stock:Stock =>
    repository
      .save(stock)

    response
        .created()
        .location(s"$base/${stock.symbol}")

  }
}



//class StockController @Inject() (repo: StockRepository) extends CrudController[String, Stock] {
//  override def base = "/stocks"
//
//  override protected val repository: Repository[String, Stock] = repo
//}