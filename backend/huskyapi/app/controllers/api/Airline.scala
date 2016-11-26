package controllers.api

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc.{Action, Controller}

@Singleton
class Airline @Inject()(config: Configuration) extends Controller {

  def oneway(start: String, from: String) = Action { implicit request =>
    Ok("")
  }

  def roundtrip(start: String, from: String, back: String, to: String) = Action { implicit request =>
    Ok("")
  }

}
