package utilites

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper, PropertyNamingStrategy}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.util.Try

trait Json {

  import Json._

  def toText: String = mapper.writeValueAsString(this)
  def toJsonNode: JsonNode = mapper.readTree(this.toText)

}

object Json {

  private val mapper = new ObjectMapper with ScalaObjectMapper
  mapper.registerModule(JodaJacksonModule)
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(Include.NON_NULL)
  mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

  def toJson(obj: AnyRef): String = Try(mapper.writeValueAsString(obj)).getOrElse("")
  def toObject[T: Manifest](content: String): Option[T] = Try(Some(mapper.readValue[T](content))).getOrElse(None)

}
