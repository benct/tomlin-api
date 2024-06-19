package no.tomlin.api.config

import no.tomlin.api.db.Table.TABLE_ROLE
import no.tomlin.api.db.Table.TABLE_USER
import no.tomlin.api.logging.LogDao
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig(private val dataSource: DataSource, private val logger: LogDao) {

    @Bean
    fun userDetailsManager(): UserDetailsManager =
        JdbcUserDetailsManager(dataSource).apply {
            setUsersByUsernameQuery("SELECT email, password, enabled FROM $TABLE_USER WHERE email = ?")
            setAuthoritiesByUsernameQuery("SELECT email, role FROM $TABLE_ROLE WHERE email = ?")
        }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .anonymous { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic(withDefaults())
            .build()

    @Bean
    fun encoder() = BCryptPasswordEncoder(4)
}
