

import kotlinx.serialization.Serializable

@Serializable
data class EmojiJson(
    val emojis: Map<String, String> = mapOf()
)