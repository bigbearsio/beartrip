package models

import utilites.Json

case class Hotel(name: String
                 , link: String
                 , photo: String
                 , price: Double) extends Json
