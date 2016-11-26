package controllers

import javax.inject.{Inject, Singleton}

import listeners.MessageListener
import play.api.mvc.{Action, Controller}

@Singleton
class Application @Inject()(listener: MessageListener) extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Hello World !"))
  }

}
