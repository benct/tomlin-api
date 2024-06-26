package no.tomlin.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.awt.image.BufferedImage

@Configuration
class WebConfig(private val clientIdInterceptor: ClientIdInterceptor) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
            .allowedOrigins("https://tomlin.no", "https://dev.tomlin.no", "http://localhost:8080")
            .maxAge(36000)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(clientIdInterceptor)
            .addPathPatterns(interceptorPaths)
    }

    @Bean
    fun imageHttpMessageConverter(): HttpMessageConverter<BufferedImage> = BufferedImageHttpMessageConverter()

    private companion object {
        val interceptorPaths = listOf(
            "admin", "authenticate", "been", "database", "file", "finn", "flight", "github", "home", "iata", "link",
            "login", "media", "note", "presence", "qr", "qrator", "rating", "reddit", "settings", "todo", "user", "weather"
        ).map { "/$it/**" }
    }
}
