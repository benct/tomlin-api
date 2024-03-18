package no.tomlin.api.common

import org.springframework.security.core.context.SecurityContextHolder

object AuthUtils {

    fun isLoggedIn(): Boolean =
        SecurityContextHolder.getContext().authentication?.isAuthenticated ?: false

    fun getUserRoles(): List<String> =
        SecurityContextHolder.getContext().authentication?.authorities.orEmpty().map { it.authority }

    fun hasRole(vararg roles: String): Boolean =
        getUserRoles().any { roles.contains(it) }
}
