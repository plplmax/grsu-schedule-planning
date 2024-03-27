package com.github.plplmax.planning.plugins.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek

class DayOfWeekSerializer : KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): DayOfWeek {
        return DayOfWeek.of(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        encoder.encodeInt(value.value)
    }
}
