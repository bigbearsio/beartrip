package models

import utilites.Json

/**
  * Created by nuboat on 11/26/2016 AD.
  */
case class Flight(name: String
                  , airline: String
                  , arrival: String
                  , arrivalDate: String
                  , arrivalTime: String
                  , departure: String
                  , departureDate: String
                  , departureTime: String
                  , photo: String
                 ) extends Json
