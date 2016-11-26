package models

import utilites.Json

case class Hotel(id: Int
                 , name: String
                 , room: String
                 , ppn: Double) extends Json
