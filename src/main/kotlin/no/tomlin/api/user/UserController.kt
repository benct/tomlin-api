package no.tomlin.api.user

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.user.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var encoder: BCryptPasswordEncoder

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
        @RequestParam(required = false) password: String?
    ): Boolean =
        userDao.storeUser(name, email, enabled, password?.let { encoder.encode(it) })
            .also { userDao.storeRole(email, USER) }

    @Secured(ADMIN)
    @DeleteMapping
    fun removeUser(@RequestParam email: String): Boolean = userDao.deleteRoles(email).and(userDao.deleteUser(email))

    @Secured(ADMIN)
    @PostMapping("/role")
    fun addRole(@RequestParam email: String, @RequestParam role: String): Boolean = userDao.storeRole(email, role)

    @Secured(ADMIN)
    @DeleteMapping("/role")
    fun removeRole(@RequestParam email: String, @RequestParam role: String): Boolean = userDao.deleteRole(email, role)
}
