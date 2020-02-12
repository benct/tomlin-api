package no.tomlin.api.github.entity

data class GitHubUser(
    val id: Long,
    val avatarUrl: String,
    val htmlUrl: String,
    val name: String? = null,
    val company: String? = null,
    val blog: String? = null,
    val location: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val publicRepos: Long,
    val publicGists: Long,
    val followers: Long,
    val following: Long
)