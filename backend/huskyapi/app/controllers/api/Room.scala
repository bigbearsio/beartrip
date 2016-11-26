package controllers.api

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}

@Singleton
class Room @Inject()() extends Controller {

  def pin(roomId: String) = Action { implicit request =>
    Ok("") as ("application/json")
  }

  def unpin(roomId: String, pinId: String) = Action { implicit request =>
    Ok("") as ("application/json")
  }

}
