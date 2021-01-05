package com.komlan.lab.market.api

import com.google.inject.Inject
import com.komlan.lab.market.domain.UserRepository
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class UserController @Inject()(repository: UserRepository) extends Controller {


  /**
   * Get all users
   */
  protected val base = "/users"
  get(base) { request: Request =>
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

  delete(base + "/:id") { request: Request =>
    repository
      .deleteById(request.getIntParam("id"))
  }

  /**
   * Create new user
   */
  post(base) { user: User =>

    val toSave = user.id match {
      case None => user.copy(id = Option(repository.getNextId))
      case _ => user
    }

    repository
      .save(toSave)

    response
      .created
      .location(s"$base/${toSave.id.get}")

  }



}
