package controllers.api

import javax.inject.{Inject, Singleton}

import facades.HotelFacade
import play.api.mvc.{Action, Controller}

@Singleton
class Hotel @Inject()(hotels: HotelFacade) extends Controller {

  def find(cityId: Int) = Action { implicit request =>
    Ok(hotels.find(cityId).toText) as ("application/json")
  }

}
