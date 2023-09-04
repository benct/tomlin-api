package no.tomlin.api.admin

import no.tomlin.api.admin.dao.UserDao
import no.tomlin.api.admin.entity.User
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.ALL_ROLES
import no.tomlin.api.common.Constants.USER
import org.springframework.security.access.annotation.Secured
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/user")
class UserController(private val userDao: UserDao, private val encoder: BCryptPasswordEncoder) {

    @Secured(ADMIN)
    @GetMapping("/me")
    fun getLoggedInUser(principal: Principal?): User? = principal?.name?.let { userDao.getUser(it) }

    @Secured(ADMIN)
    @GetMapping
    fun getUsers(): List<User> = userDao.getUsers()

    @Secured(ADMIN)
    @PostMapping
    fun storeUser(
        @RequestParam name: String,
        @RequestParam email: String,
        @RequestParam enabled: Boolean,
        @RequestParam password: String?
    ): Boolean =
        userDao.storeUser(name, email, enabled, password?.let { encoder.encode(it) })
            .also { userDao.storeRole(email, USER) }

    @Secured(ADMIN)
    @DeleteMapping
    fun removeUser(@RequestParam email: String): Boolean = userDao.deleteRoles(email).and(userDao.deleteUser(email))

    @Secured(ADMIN)
    @GetMapping("/role")
    fun getRoles(): List<String> = ALL_ROLES.toList()

    @Secured(ADMIN)
    @PostMapping("/role")
    fun addRole(@RequestParam email: String, @RequestParam role: String): Boolean =
        if (ALL_ROLES.contains(role.uppercase())) userDao.storeRole(email, role.uppercase()) else false

    @Secured(ADMIN)
    @DeleteMapping("/role")
    fun removeRole(@RequestParam email: String, @RequestParam role: String): Boolean =
        userDao.deleteRole(email, role.uppercase())
}
