package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
    premisesPrices: List[(Long, Double)] // (id, price)
)

object Contract {
  implicit val jsonDecoder: Decoder[Contract] = deriveDecoder
  implicit val jsonEncoder: Encoder[Contract] = deriveEncoder
}
