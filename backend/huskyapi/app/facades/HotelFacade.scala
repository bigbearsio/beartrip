package facades

import javax.inject.Singleton

import models.{Hotel, Hotels}

@Singleton
class HotelFacade {

  def find(city: Int): Hotels = {
    Hotels(List(Hotel(id = 1, "", "", 1000.00)
      , Hotel(id = 2, "", "", 800.00)
      , Hotel(id = 3, "", "", 1200.00)))
  }

}
