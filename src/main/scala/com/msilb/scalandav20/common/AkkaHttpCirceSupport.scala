package com.msilb.scalandav20.common

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import io.circe._

object AkkaHttpCirceSupport extends AkkaHttpCirceSupport

trait AkkaHttpCirceSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller = Marshaller.stringMarshaller(`application/json`)

  implicit def circeUnmarshaller[A](implicit decoder: Decoder[A]): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(jawn.decode(_).fold(throw _, identity))

  implicit def circeToEntityMarshaller[A](implicit encoder: Encoder[A],
                                          printer: Json => String = Printer.noSpaces.copy(dropNullValues = true).pretty): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(printer).compose(encoder.apply)
}
