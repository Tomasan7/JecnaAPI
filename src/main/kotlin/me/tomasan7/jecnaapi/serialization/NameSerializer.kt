package me.tomasan7.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.tomasan7.jecnaapi.util.Name

object NameSerializer : KSerializer<Name>
{
    override val descriptor = PrimitiveSerialDescriptor("Name", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Name) = encoder.encodeString(
        if (value.short != null)
            value.full + DIVIDER + value.short
        else
            value.full)

    override fun deserialize(decoder: Decoder): Name
    {
        val string = decoder.decodeString()
        val split = string.split(DIVIDER, limit = 2)
        return Name(split[0], split.getOrNull(1))
    }

    private const val DIVIDER = "\$"
}
