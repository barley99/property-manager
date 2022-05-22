package domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDate

final case class Contract(
    id: Option[Long],
    number: String,
    tenant: Long,
    agreementDate: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate,
    paymentDate: LocalDate,
    utilitiesInPrice: Boolean,
    premisesPrices: Option[List[(Long, Double)]] // (id, price)
)

object Contract {
  implicit val jsonDecoder: Decoder[Contract] = deriveDecoder
  implicit val jsonEncoder: Encoder[Contract] = deriveEncoder
}
