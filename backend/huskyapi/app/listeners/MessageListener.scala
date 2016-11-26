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

  ref.addChildEventListener( new ChildEventListener {
    override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = {
      val document = dataSnapshot.getValue().asInstanceOf[util.HashMap[String, String]]
      val message = Message(name = document.get("name")
        , text = document.get("text")
        , photoUrl = document.get("photoUrl")
        , created = document.get("created"))

      println(message)
    }

    override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = ???

    override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = ???

    override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = ???

    override def onCancelled(databaseError: DatabaseError): Unit = ???
  })
//  ref.addValueEventListener(new ValueEventListener {
//
//    override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
//      val document = dataSnapshot.getValue()
//      println(document)
//    }
//
//    override def onCancelled(databaseError: DatabaseError): Unit = {
//      println("not implement")
//    }
//  })

}
