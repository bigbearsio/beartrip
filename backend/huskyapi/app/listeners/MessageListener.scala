package listeners

import java.io.FileInputStream
import java.util
import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.google.firebase.database._
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import models.{Flight, Hotel, JMessage, Message}
import play.api.Configuration


@Singleton
class MessageListener @Inject()(config: Configuration) {

  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  var isFirst = true

  val options = new FirebaseOptions.Builder()
    //.setServiceAccount(new FileInputStream(config.getString("huskytrip.key").get))
    .setServiceAccount(new FileInputStream("/Users/prapat/huskydbkey.json"))
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
    setMockDecidingFlights()
    huskiResponse("โฮ่งๆ")
  }

  private def suggestHotels(): Unit = {
    setMockDecidingHotels()
    huskiResponse("Roger")
  }

  private def setMockDecidingHotels(): Unit = {

    val idD = UUID.randomUUID().toString
    val id0 = UUID.randomUUID().toString
    val id1 = UUID.randomUUID().toString
    val id2 = UUID.randomUUID().toString
    val id3 = UUID.randomUUID().toString

    val hotel1 = new Hotel(id1
      ,"Duangtawan Hotel"
      , "https://www.agoda.com/duangtawan-hotel/hotel/chiang-mai-th.html?checkin=2016-12-24&los=2&adults=2&rooms=1&cid=-1&searchrequestid=2cb15ad2-47fb-42dc-8732-de7f0ca64ce1"
      , "http://pix1.agoda.net/hotelImages/489/48944/48944_15072010480032592224.jpg"
      , 2668d, 0)
    val hotel2 = new Hotel(id2
      ,"The Imperial Mae Ping Hotel"
      , "https://www.agoda.com/the-imperial-mae-ping-hotel/hotel/chiang-mai-th.html?checkin=2016-12-24&los=2&adults=2&rooms=1&cid=-1&searchrequestid=2cb15ad2-47fb-42dc-8732-de7f0ca64ce1"
      , "https://pix3.agoda.net/hotelImages/107/10748/10748_16040716330041378045.jpg?s=1100x825"
      , 2458d, 0)
    val hotel3 = new Hotel(id3
      ,"Le Meridien Chiang Mai Hotel"
      , "https://www.agoda.com/le-meridien-chiang-mai-hotel/hotel/chiang-mai-th.html?checkin=2016-12-24&los=2&adults=2&rooms=1&cid=-1&searchrequestid=2cb15ad2-47fb-42dc-8732-de7f0ca64ce1"
      , "https://pix2.agoda.net/hotelImages/194/194958/194958_16052705370042776689.jpg?s=1100x825"
      , 8319d, 0)

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

  private def setMockDecidingFlights(): Unit = {
    val idD = UUID.randomUUID().toString
    val idF = UUID.randomUUID().toString
    val id1 = UUID.randomUUID().toString
    val id2 = UUID.randomUUID().toString
    val id3 = UUID.randomUUID().toString

    val flight1 = new Flight(id1
      , "Thai Airways"
      , "TG"
      , "CNX-BKK"
      , "2016-12-26"
      , "6:15a-7:30a"
      , "BKK-CNX"
      , "2016-12-24"
      , "10:20p-11:35p"
      , "https://a1.r9cdn.net/res/images/air/2x/TG.png"
      , 3750d
      , 0)

    val flight2 = new Flight(id2
      , "Thai Smile"
      , "WE"
      , "CNX-BKK"
      , "2016-12-26"
      , "6:15a-7:30a"
      , "BKK-CNX"
      , "2016-12-24"
      , "10:20p-11:35p"
      , "https://a1.r9cdn.net/res/images/air/2x/WE.png"
      , 4098d
      , 0)

    val flight3 = new Flight(id3
      , "Bangkok Airways"
      , "PG"
      , "CNX-BKK"
      , "2016-12-26"
      , "9:15p-10:30p"
      , "BKK-CNX"
      , "2016-12-24"
      , "06:15a-07:30a"
      , "https://a1.r9cdn.net/res/images/air/2x/PG.png"
      , 4471d
      , 0)

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


  val huskyImage = "https://huskytrip.firebaseapp.com/images/app-icon-192.png"
  val huskyName = "Husky"
  private def huskiResponse(text:String): Unit = {

    val jmessage = new JMessage(sdf.format(new java.util.Date())
      , text
      , huskyImage
      , huskyName)

    FirebaseDatabase.getInstance()
      .getReference("messages")
      .push()
      .setValue(jmessage)
  }


}
