package listeners

import java.io.FileInputStream
import java.util
import javax.inject.{Inject, Singleton}

import com.google.firebase.database._
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import models.{Flight, Hotel, Message}
import play.api.Configuration


@Singleton
class MessageListener @Inject()(config: Configuration) {

  var isFirst = true

  val options = new FirebaseOptions.Builder()
    .setServiceAccount(new FileInputStream(config.getString("huskytrip.key").get))
    .setDatabaseUrl("https://huskytrip.firebaseio.com/")
    .build()

  FirebaseApp.initializeApp(options)

  val ref = FirebaseDatabase
    .getInstance()
    .getReference("messages")

  ref.limitToLast(1)
    .addChildEventListener(new ChildEventListener {
      override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = {
        if (isFirst) {
          isFirst = false
          return
        }

        val message = parseToMessage(dataSnapshot)
        println(s"Added: ${message}")
        suggestion(message)
      }

      override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = {}

      override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = {}

      override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = {
        val message = parseToMessage(dataSnapshot)
        suggestion(message)
      }

      override def onCancelled(databaseError: DatabaseError): Unit = {}
    })

  private def parseToMessage(data: DataSnapshot): Message = {
    val document = data.getValue().asInstanceOf[util.HashMap[String, String]]
    Message(name = document.get("name")
      , text = document.get("text")
      , photoUrl = document.get("photoUrl")
      , created = document.get("created"))
  }

  private def suggestion(message: Message): Unit = {
    val pattern = message.text.toLowerCase
    if (pattern.contains("เครื่องบิน")
      || pattern.contains("เดินทาง")) {

      suggestFlights()
    }

    if (pattern.contains("โรงแรม")
      || pattern.contains("ที่พัก")) {

      suggestHotels()
    }
  }

  private def suggestFlights(): Unit = {
    //println("TODO: นั่งเครืองบินไปสิ")
    val flight1 = Flight(name = "Thai Airways"
      , airline = "CNX-BKK"
      , arrival = "2016-12-26"
      , arrivalDate = "10:00-11:20"
      , arrivalTime = "BKK-CNX"
      , departure = "2016-12-24"
      , departureDate = "17:30-18:50"
      , departureTime = "Thai Air"
      , photo = "https://a1.r9cdn.net/res/images/air/2x/TG.png?v=3ffd422baac0a417e639eab051d1a7ebd59aaf5d")

    val idD = (new java.util.Date()).getTime()
    val idF = (new java.util.Date()).getTime() + 2
    val id1 = (new java.util.Date()).getTime() + 4
    val id2 = (new java.util.Date()).getTime() + 6
    val id3 = (new java.util.Date()).getTime() + 8

    val flights = new java.util.HashMap[Long, Flight]()
    flights.put(id1, flight1)
    flights.put(id2, flight1)
    flights.put(id3, flight1)

    val deciding = new java.util.HashMap[Long, Object]()
    deciding.put(idD, flights)

    FirebaseDatabase.getInstance().getReference("/").child("deciding").setValue(deciding)
  }

  private def suggestHotels(): Unit = {
    //println("TODO: จองอโกด้าสิ")

    val hotel1 = Hotel(name = "Duangtawan Hotel"
      , photo = "http://pix1.agoda.net/hotelImages/489/48944/48944_15072010480032592224.jpg"
      , price = 1760
      , link = "")
    val hotel2 = Hotel(name = "The Imperial Mae Ping Hotel"
      , photo = "http://pix3.agoda.net/hotelImages/107/10748/10748_16040716330041378045.jpg"
      , price = 2010
      , link = "")
    val hotel3 = Hotel(name = "Duangtawan Hotel"
      , photo = "//pix1.agoda.net/hotelImages/276/276670/276670_15070709560031781137.jpg"
      , price = 2272
      , link = "")

    val idD = new java.util.Date().getTime()
    val idH = new java.util.Date().getTime() + 2
    val id1 = new java.util.Date().getTime() + 4
    val id2 = new java.util.Date().getTime() + 6
    val id3 = new java.util.Date().getTime() + 8
    val hotels = new java.util.HashMap[Long, Hotel]()
    hotels.put(id1, hotel1)
    hotels.put(id2, hotel2)
    hotels.put(id3, hotel3)
    val deciding = new java.util.HashMap[Long, Object]()
    deciding.put(idH, hotels)

    FirebaseDatabase.getInstance()
      .getReference("/")
      .child("deciding")
      .setValue(deciding)
  }

}
