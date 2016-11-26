package listeners

import java.io.FileInputStream
import java.util
import javax.inject.Singleton

import com.google.firebase.database._
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import models.Message

@Singleton
class MessageListener {

  val options = new FirebaseOptions.Builder()
    .setServiceAccount(new FileInputStream("/Users/nuboat/Bigbears/huskytrip-firebase-adminsdk-1u0w9-b0c93ab2d1.json"))
    .setDatabaseUrl("https://huskytrip.firebaseio.com/")
    .build()

  FirebaseApp.initializeApp(options)

  val ref = FirebaseDatabase
    .getInstance()
    .getReference("messages")

  ref.limitToLast(1)
  ref.addChildEventListener(new ChildEventListener {
    override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = {
      val message = parseToMessage(dataSnapshot)
      suggestion(message)
      println(s"Added: ${message}")
    }

    override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = {}

    override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = {}

    override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = {
      val message = parseToMessage(dataSnapshot)
      suggestion(message)
      println(s"Changed: ${message}")
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
      || pattern.contains("การเดินทาง")) {
      println("นั่งเครืองบินไปสิ")
    }

    if (pattern.contains("โรงแรม")
      || pattern.contains("ที่พัก")) {
      println("จองอโกด้าสิ")
    }
  }

}
