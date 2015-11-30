package org.zalando.jsonapi.json

import org.zalando.jsonapi.model.RootObject._
import org.zalando.jsonapi.model._
import spray.json._

trait JsonapiJsonFormat {
  self: DefaultJsonProtocol ⇒

  /**
   * Spray-JSON format for serializing [[org.zalando.jsonapi.model.RootObject]] to Jsonapi.
   */
  implicit val rootObjectWriter: RootJsonWriter[RootObject] = new RootJsonWriter[RootObject] {
    override def write(rootObject: RootObject): JsValue = {

      val data = rootObject.data match {
        case Some(d) ⇒ Some("data" -> d.toJson)
        case None    ⇒ None
      }

      val links = rootObject.links match {
        case Some(l) ⇒ Some("links" -> l.toJson)
        case None    ⇒ None
      }

      val meta = rootObject.meta match {
        case Some(m) ⇒ Some("meta" -> m.toJson)
        case None    ⇒ None
      }

      val errors = rootObject.errors match {
        case Some(e) ⇒ Some("errors" -> e.toJson)
        case None    ⇒ None
      }

      JsObject(
        Map()
          ++ data
          ++ links
          ++ meta
          ++ errors
      )
    }
  }

  implicit val dataWriter: RootJsonWriter[Data] = new RootJsonWriter[Data] {
    override def write(data: Data): JsValue = {
      data match {
        case ro: ResourceObject            ⇒ ro.toJson
        case rio: ResourceIdentifierObject ⇒ rio.toJson
        case ResourceObjects(resourceObjects) ⇒
          val objects = resourceObjects map (ro ⇒ ro.toJson)
          JsArray(objects.toVector)
        case ResourceIdentifierObjects(resourceIdentifierObjects) ⇒
          val objects = resourceIdentifierObjects map (rio ⇒ rio.toJson)
          JsArray(objects.toVector)
      }
    }
  }

  implicit val resourceObjectWriter: RootJsonWriter[ResourceObject] = new RootJsonWriter[ResourceObject] {
    override def write(resourceObject: ResourceObject): JsValue = {

      // check if there are some attributes to add
      val attributes = resourceObject.attributes match {
        case Some(a) ⇒ Some("attributes" -> a.toJson)
        case None    ⇒ None
      }

      // check if there are some links to add
      val links = resourceObject.links match {
        case Some(l) ⇒ Some("links" -> l.toJson)
        case None    ⇒ None
      }

      // check if there are some meta object to add
      val meta = resourceObject.meta match {
        case Some(m) ⇒ Some("meta" -> m.toJson)
        case None    ⇒ None
      }

      JsObject(
        (
          Seq(
            "type" -> resourceObject.`type`.toJson,
            "id" -> resourceObject.id.toJson
          ) ++ attributes
            ++ links
            ++ meta
        ).toMap
      )
    }
  }

  implicit val resourceIdentifierObjectWriter: RootJsonWriter[ResourceIdentifierObject] = new RootJsonWriter[ResourceIdentifierObject] {
    override def write(resourceIdentifierObject: ResourceIdentifierObject): JsValue = {
      JsObject(Map(
        "type" -> resourceIdentifierObject.`type`.toJson,
        "id" -> resourceIdentifierObject.id.toJson
      ))
    }
  }

  /**
   * Spray-JSON format for serializing Jsonapi [[org.zalando.jsonapi.model.Attributes]].
   */
  implicit val attributesWriter: RootJsonWriter[Attributes] = new RootJsonWriter[Attributes] {
    override def write(attributes: Attributes): JsValue = {
      val fields = attributes map (p ⇒ p.name -> p.value.toJson)
      JsObject(fields: _*)
    }
  }

  /**
   * Spray-JSON format for serializing Jsonapi [[org.zalando.jsonapi.model.Meta]].
   */
  implicit val metaWriter: RootJsonWriter[Meta] = new RootJsonWriter[Meta] {
    override def write(meta: Meta): JsValue = {
      val fields = meta map (m ⇒ m.name -> m.value.toJson)
      JsObject(fields: _*)
    }
  }

  implicit val errorsWriter: RootJsonWriter[Errors] = new RootJsonWriter[Errors] {
    override def write(errors: Errors): JsValue = {
      val objects = errors map (e ⇒ e.toJson)
      JsArray(objects.toVector)
    }
  }

  /**
   * Spray-JSON format for serializing Jsonapi [[org.zalando.jsonapi.model.Meta]].
   */
  implicit val errorWriter: RootJsonWriter[Error] = new RootJsonWriter[Error] {
    override def write(error: Error): JsObject = {

      // check if there are some links to add
      val links = error.links match {
        case Some(l) ⇒ Some("links" -> l.toJson)
        case None    ⇒ None
      }

      val meta = error.meta match {
        case Some(m) ⇒ Some("meta" -> m.toJson)
        case None    ⇒ None
      }

      JsObject(Map(
        "id" -> error.id.getOrElse("").toJson,
        "status" -> error.status.getOrElse("").toJson,
        "code" -> error.code.getOrElse("").toJson,
        "title" -> error.title.getOrElse("").toJson,
        "detail" -> error.detail.getOrElse("").toJson)
        //"source" -> error.source.getOrElse("").toJson,
        ++ links
        ++ meta
      )
    }
  }

  /**
   * Spray-JSON format for serializing [[org.zalando.jsonapi.model.JsonApiObject]] to Jsonapi.
   */
  implicit val jsonApiObjectValueWriter: JsonFormat[JsonApiObject.Value] = lazyFormat(new JsonFormat[JsonApiObject.Value] {
    override def write(oValue: JsonApiObject.Value): JsValue = {
      oValue match {
        case JsonApiObject.StringValue(s)   ⇒ s.toJson
        case JsonApiObject.NumberValue(n)   ⇒ n.toJson
        case JsonApiObject.BooleanValue(b)  ⇒ b.toJson
        case JsonApiObject.JsObjectValue(o) ⇒ o.toJson
        case JsonApiObject.JsArrayValue(a)  ⇒ a.toJson
        case JsonApiObject.NullValue        ⇒ JsNull
      }
    }

    // We don't need it for now
    override def read(json: JsValue): JsonApiObject.Value = ???
  })

  /**
   * Spray-JSON format for serializing Jsonapi [[org.zalando.jsonapi.model.Links]].
   */
  implicit val linksWriter: RootJsonWriter[Links] = new RootJsonWriter[Links] {
    override def write(links: Links): JsValue = {
      val fields = links map (l ⇒
        l.linkOption match {
          case Link.Self(url)    ⇒ "self" -> url.toJson
          case Link.About(url)   ⇒ "about" -> url.toJson
          case Link.First(url)   ⇒ "first" -> url.toJson
          case Link.Last(url)    ⇒ "last" -> url.toJson
          case Link.Next(url)    ⇒ "next" -> url.toJson
          case Link.Prev(url)    ⇒ "prev" -> url.toJson
          case Link.Related(url) ⇒ "related" -> url.toJson
        }
      )
      JsObject(fields: _*)
    }
  }
}
