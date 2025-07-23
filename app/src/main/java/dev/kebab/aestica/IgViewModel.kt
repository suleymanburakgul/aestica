package dev.kebab.aestica

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kebab.aestica.data.Event
import dev.kebab.aestica.data.model.CommentModel
import dev.kebab.aestica.data.model.PostModel
import dev.kebab.aestica.data.model.UserModel
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserModel?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostModel>>(listOf())

    val searchedPosts = mutableStateOf<List<PostModel>>(listOf())
    val searchedPostProgress = mutableStateOf(false)

    private val _selectedPost = mutableStateOf<PostModel?>(null)
    val selectedPost: State<PostModel?> = _selectedPost

    val postsFeed = mutableStateOf<List<PostModel>>(listOf())
    val postFeedProgress = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentModel>>(listOf())
    val commentsProgress = mutableStateOf(false)

    val followers = mutableStateOf(0)


    init {
      //  auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun selectPost(post: PostModel) {
        _selectedPost.value = post
    }

    fun onSignup(
        username: String,
        email: String,
        pass: String
    ) {
        if(username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if(documents.size() > 0) {
                    handleException(
                        customMessage = "This username already exists"
                    )
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {  }
    }

    fun onLogin(
        email: String,
        pass: String
    ) {
        if(email.isEmpty() || pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        //handleException(customMessage = "Login Success")
                        getUserData(uid)
                    }
                } else {
                    handleException(task.exception, "Login failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Login failed")
                inProgress.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserModel(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let {uid ->
            inProgress.value = true
            db.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if(it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        db.collection(USERS).document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(
        uid: String
    ) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserModel>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(user?.userId)
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot retrieve user data")
                inProgress.value = false
            }
    }

    fun handleException(
        exception: Exception? = null,
        customMessage: String = ""
    ) {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if(customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
    }

    fun updateProfileData(
        name: String,
        username: String,
        bio: String
    ) {
        createOrUpdateProfile(name, username, bio)
    }

    private fun UploadImage(
        uri: Uri,
        onSuccess: (Uri) -> Unit
    ){
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exc ->
                handleException(exception = exc)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri) {
        UploadImage(
            uri = uri,
            onSuccess = {
                createOrUpdateProfile(imageUrl = it.toString())
                updatePostUserImageData(it.toString())
            }
        )
    }

    private fun updatePostUserImageData(imageUrl: String) {
        val currentUid = auth.currentUser?.uid
        db.collection(POSTS).whereEqualTo("userId", currentUid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostModel>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }
                if(refs.isNotEmpty()) {
                    db.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }
                        .addOnSuccessListener {
                            refreshPosts()
                        }
                }
            }
    }

    fun onLogout(){
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
        comments.value = listOf()
    }

    fun onNewPost(
        uri: Uri,
        description: String,
        onPostSuccess: () -> Unit
    ){
        UploadImage(
            uri = uri,
            onSuccess = {
                onCreatePost(it, description, onPostSuccess)
            }
        )
    }

    private fun onCreatePost(
        imageUri: Uri,
        description: String,
        onPostSuccess: () -> Unit
    ) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if(currentUid != null) {

            val postUuid = UUID.randomUUID().toString()

            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "a", "in",
                "that", "have", "I", "it", "for", "not", "on", "with", "he", "as", "you", "do)"
            )
            val searchTerms = description
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            val post = PostModel(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )

            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event("Post successfully created")
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess()
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create post")
                    inProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: username unavailable")
            onLogout()
            inProgress.value = false
        }
    }

    private fun refreshPosts(){
        val currentUid = auth.currentUser?.uid
        if(currentUid != null) {
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot fetch posts")
                    refreshPostsProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: username unavailable")
            onLogout()
            inProgress.value = false
        }
    }

    private fun convertPosts(
        documents: QuerySnapshot,
        outState: MutableState<List<PostModel>>
    ) {
        val newPosts = mutableListOf<PostModel>()
        documents.forEach { doc ->
            val post = doc.toObject<PostModel>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String) {
        if(searchTerm.isNotEmpty()) {
            searchedPostProgress.value = true
            db.collection(POSTS)
                .whereArrayContains("searchTerms", searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPosts)
                    searchedPostProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot search posts")
                    searchedPostProgress.value = false
                }
        }
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if(following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser).update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed() {
        val following = userData.value?.following
        if(!following.isNullOrEmpty()) {
            postFeedProgress.value = true
            db.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, postsFeed)
                    if(postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot get feed")
                    postFeedProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed() {
        postFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000 // 1 day in milis

        db.collection(POSTS)
            .whereGreaterThan("time", currentTime - difference)
            .get()
            .addOnSuccessListener { documents ->
                convertPosts(documents, postsFeed)
                postFeedProgress.value = false
            }
            .addOnFailureListener {  exc ->
                handleException(exc, "Cannot get feed")
                postFeedProgress.value = false
            }
    }

    fun onLikePost(
        postData: PostModel
    ) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if(likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                    newLikes.remove(userId)
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { posId ->
                    db.collection(POSTS).document(posId).update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "Cannot like post")
                        }
                }
            }
        }
    }

    fun createComment(
        postId: String,
        text: String
    ) {
        userData.value?.username.let { username ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentModel(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timeStamp = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getComments(postId)
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create comment")
                }
        }
    }

    fun getComments(postId: String?) {
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo("postId", postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentModel>()
                documents.forEach { doc ->
                    val comment = doc.toObject<CommentModel>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending {
                    it.timeStamp
                }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot get comments")
                commentsProgress.value = false
            }
    }

    private fun getFollowers(uid: String?) {
        db.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                followers.value = documents.size()
            }
    }
 }