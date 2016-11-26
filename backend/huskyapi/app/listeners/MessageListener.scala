package listeners

import java.io.FileInputStream
import java.util
import javax.inject.{Inject, Singleton}

import com.google.firebase.database._
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import models.Message
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
    println("TODO: นั่งเครืองบินไปสิ")
  }

  private def suggestHotels(): Unit = {
    println("TODO: จองอโกด้าสิ")
  }

}
