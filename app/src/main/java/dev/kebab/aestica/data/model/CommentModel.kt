package dev.kebab.aestica.data.model

data class CommentModel(
    val commentId: String? = null,
    val postId: String? = null,
    val username: String? = null,
    val text: String? = null,
    val timeStamp: Long? = null,
)
