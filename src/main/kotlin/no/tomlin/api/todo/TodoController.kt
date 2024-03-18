package no.tomlin.api.todo

import no.tomlin.api.common.AuthUtils.hasRole
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.todo.TodoDao.Todo
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/todo")
class TodoController(private val todoDao: TodoDao) {

    @GetMapping
    fun list(): List<Todo> =
        todoDao.get().let { todos ->
            if (hasRole(ADMIN, PRIVATE)) {
                todos
            } else {
                todos.map {
                    if (it.private) it.copy(title = "hidden", text = null) else it
                }
            }
        }

    @Secured(ADMIN, PRIVATE)
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Todo? =
        todoDao.get(id)

    @Secured(ADMIN, PRIVATE)
    @PostMapping
    fun save(
        @RequestParam id: Long?,
        @RequestParam title: String,
        @RequestParam text: String?,
        @RequestParam private: Boolean?,
    ): Boolean =
        todoDao.save(id, title, text.nullIfBlank(), private ?: false)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/check/{id}")
    fun setOrder(@PathVariable id: Long, @RequestParam checked: Boolean): Boolean =
        todoDao.setChecked(id, checked)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/order/{id}")
    fun setOrder(@PathVariable id: Long, @RequestParam order: Int): Boolean =
        todoDao.setOrder(id, order)

    @Secured(ADMIN, PRIVATE)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean =
        todoDao.delete(id)
}