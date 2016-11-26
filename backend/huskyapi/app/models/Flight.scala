package models

import utilites.Json

case class Flight(id: String, name: String, seat: String, price: Double) extends Json
