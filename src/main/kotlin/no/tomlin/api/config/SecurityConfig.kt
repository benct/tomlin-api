package no.tomlin.api.config

import no.tomlin.api.db.Table.TABLE_ROLE
import no.tomlin.api.db.Table.TABLE_USER
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
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
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig(private val dataSource: DataSource) {

    @Bean
    fun userDetailsManager(): UserDetailsManager =
        JdbcUserDetailsManager(dataSource).apply {
            setUsersByUsernameQuery("SELECT email, password, enabled FROM $TABLE_USER WHERE email = ?")
            setAuthoritiesByUsernameQuery("SELECT email, role FROM $TABLE_ROLE WHERE email = ?")
        }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .anonymous().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic()
            .and()
            .build()

    @Bean
    fun encoder() = BCryptPasswordEncoder(4)
}
