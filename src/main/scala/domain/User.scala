package domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class User(
    id: Option[Long],
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    hash: String,
    role: String
)

object User {
  implicit val jsonDecoder: Decoder[User] = deriveDecoder
  implicit val jsonEncoder: Encoder[User] = deriveEncoder
}
