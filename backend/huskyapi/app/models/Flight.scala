package models

import utilites.Json

/**
  * Created by nuboat on 11/26/2016 AD.
  */
case class Flight(id: String, name: String, seat: String, price: Double) extends Json
