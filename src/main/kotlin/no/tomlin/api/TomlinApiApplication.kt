package no.tomlin.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TomlinApiApplication

fun main(args: Array<String>) {
    runApplication<TomlinApiApplication>(*args)
}
