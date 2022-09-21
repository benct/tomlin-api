package no.tomlin.api.github

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import no.tomlin.api.common.JsonUtils.parseJson
import no.tomlin.api.github.entity.GitHubRepo
import no.tomlin.api.github.entity.GitHubUser
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class GitHubService(val fetcher: HttpFetcher = fetcher(BASE_URL)) {

    @CachePut("github")
    fun getUserData(): Map<String, Any> {
        val responses = listOf(USER_PATH, REPO_PATH)
            .parallelStream()
            .map { fetcher.get(it).readBody() }
            .collect(Collectors.toList())

        val userData = responses.first().parseJson<GitHubUser>(SNAKE_CASE)
        val repoData = responses.last().parseJson<GitHubRepos>(SNAKE_CASE)

        return mapOf(
            "user" to userData,
            "stars" to repoData.sumOf { it.stargazersCount.toInt() },
            "top" to repoData.filter { !it.fork }.sortedByDescending { it.stargazersCount }.take(3),
            "featured" to repoData.filter { FEATURED.contains(it.name) }
        )
    }

    class GitHubRepos(elements: Collection<GitHubRepo>) : ArrayList<GitHubRepo>(elements)

    companion object {
        const val BASE_URL = "https://api.github.com"
        const val USER_PATH = "/users/benct"
        const val REPO_PATH = "/users/benct/repos"

        val FEATURED = listOf("tomlin-web", "iata-utils", "dotfiles")
    }
}