package me.tomasan7.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.tomasan7.jecnaapi.util.SchoolYear

object SchoolYearSerializer : KSerializer<SchoolYear>
{
    override val descriptor = PrimitiveSerialDescriptor("SchoolYear", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SchoolYear) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder) = SchoolYear.fromString(decoder.decodeString())
}
