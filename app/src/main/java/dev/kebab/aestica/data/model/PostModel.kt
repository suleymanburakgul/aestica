package dev.kebab.aestica.data.model

data class PostModel(
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val userImage: String? = null,
    val postImage: String? = null,
    val postDescription: String? = null,
    val time: Long? = null,
    var likes: List<String>? = null,
    val searchTerms: List<String>? = null
)