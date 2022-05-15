package cbdc.corda.middleware.rest.serialize

import cbdc.corda.state.generic.Amount
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.boot.jackson.JsonComponent
import java.math.BigDecimal

@JsonComponent
class AmountSerializer : StdSerializer<Amount<*>>(Amount::class.java) {
    override fun serialize(value: Amount<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeObject(
            SignedAmount(
                quantity = value.toDecimal(),
                token = value.token as String
            )
        )
    }
}

@JsonComponent
class AmountDeserializer : StdDeserializer<Amount<*>>(Amount::class.java) {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Amount<*> {
        val json = parser.readValueAs(SignedAmount::class.java)
        return Amount.fromDecimal(json.quantity, json.token)
    }
}

/**
 * In some cases we need to represent amounts that can be negative, but Amount only supports non-negative quantities.
 *
 * Also this class is used in the Amount serializer.
 */
data class SignedAmount(
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    val quantity: BigDecimal,
    val token: String
) {
    companion object {
        fun from(amount: Amount<String>) = SignedAmount(amount.toDecimal(), amount.token)
    }
}
