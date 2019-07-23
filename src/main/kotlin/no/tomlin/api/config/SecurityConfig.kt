package no.tomlin.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic()
//            .and()
//            .requiresChannel().anyRequest().requiresSecure()
    }

//    override fun configure(auth: AuthenticationManagerBuilder) {
//        auth.inMemoryAuthentication()
//            .passwordEncoder(BCryptPasswordEncoder(12))
//            .withUser(username)
//            .password(password)
//            .roles("ADMIN", "USER")
//    }
}
