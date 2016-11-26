package listeners

import java.io.FileInputStream
import java.util
import java.util.UUID
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
      || pattern.contains("เดินทาง")
      || pattern.contains("flight")
      || pattern.contains("kayaak")) {

      suggestFlights()
    }

    if (pattern.contains("โรงแรม")
      || pattern.contains("ที่พัก")
      || pattern.contains("hotel")
      || pattern.contains("agoda")) {

      suggestHotels()
    }
  }

  private def suggestFlights(): Unit = {
    //println("TODO: นั่งเครืองบินไปสิ")
    val idD = UUID.randomUUID().toString
    val idF = UUID.randomUUID().toString
    val id1 = UUID.randomUUID().toString
    val id2 = UUID.randomUUID().toString
    val id3 = UUID.randomUUID().toString

    val flight1 = new Flight(id1
      , "Thai Airways"
      , "Thai Air"
      , "CNX-BKK"
      , "2016-12-26"
      , "10:00-11:20"
      , "BKK-CNX"
      , "2016-12-24"
      , "17:30-18:50"
      , "https://a1.r9cdn.net/res/images/air/2x/TG.png?v=3ffd422baac0a417e639eab051d1a7ebd59aaf5d")

    val flight2 = new Flight(id2
      , "Thai Airways"
      , "Thai Air"
      , "CNX-BKK"
      , "2016-12-26"
      , "10:00-11:20"
      , "BKK-CNX"
      , "2016-12-24"
      , "17:30-18:50"
      , "https://a1.r9cdn.net/res/images/air/2x/TG.png?v=3ffd422baac0a417e639eab051d1a7ebd59aaf5d")

    val flight3 = new Flight(id3
      , "Thai Airways"
      , "Thai Air"
      , "CNX-BKK"
      , "2016-12-26"
      , "10:00-11:20"
      , "BKK-CNX"
      , "2016-12-24"
      , "17:30-18:50"
      , "https://a1.r9cdn.net/res/images/air/2x/TG.png?v=3ffd422baac0a417e639eab051d1a7ebd59aaf5d")

    val deciding = new util.HashMap[String, Flight]()
    deciding.put(id1, flight1)
    deciding.put(id2, flight2)
    deciding.put(id3, flight3)

    //val deciding = new util.HashMap[String, util.HashMap[String, Flight]]()
    //deciding.put(idD, flights)

    FirebaseDatabase.getInstance()
      .getReference("deciding")
      .setValue(deciding)
  }

  private def suggestHotels(): Unit = {
    //println("TODO: จองอโกด้าสิ")

    val idD = UUID.randomUUID().toString
    val id0 = UUID.randomUUID().toString
    val id1 = UUID.randomUUID().toString
    val id2 = UUID.randomUUID().toString
    val id3 = UUID.randomUUID().toString

    val hotel1 = new Hotel(id1
      ,"Duangtawan Hotel"
      , ""
      , "http://pix1.agoda.net/hotelImages/489/48944/48944_15072010480032592224.jpg"
      , 1760d, 0)
    val hotel2 = new Hotel(id2
      ,"The Imperial Mae Ping Hotel"
      , ""
      , "http://pix3.agoda.net/hotelImages/107/10748/10748_16040716330041378045.jpg"
      , 2010d, 0)
    val hotel3 = new Hotel(id3
      ,"Duangtawan Hotel"
      , ""
      , "//pix1.agoda.net/hotelImages/276/276670/276670_15070709560031781137.jpg"
      , 2272d, 0)

    val deciding = new util.HashMap[String, Hotel]()
    deciding.put(id1, hotel1)
    deciding.put(id2, hotel2)
    deciding.put(id3, hotel3)

    //val deciding = new util.HashMap[String, util.HashMap[String, Hotel]]()
    //deciding.put(id0, hotels)

    FirebaseDatabase.getInstance()
      .getReference("deciding")
      .setValue(deciding)
  }

}
