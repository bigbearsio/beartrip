package models

import utilites.Json

/**
  * Created by nuboat on 11/26/2016 AD.
  */
case class Message(name: String, text: String, photoUrl: String, created: String) extends Json
