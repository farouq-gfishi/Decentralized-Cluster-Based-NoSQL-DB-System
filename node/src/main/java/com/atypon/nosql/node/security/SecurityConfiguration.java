package com.atypon.nosql.node.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfiguration {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        inMemoryUserDetailsManager.createUser(
                User.builder()
                        .username("admin")
                        .password("{bcrypt}$2a$12$41r/Lt04E5clXAzS5NA8IusV9zsCqKbPpOeSWSkmxhcjT.LMOC9.a")
                        .roles("ADMIN")
                        .build()
        );
        return inMemoryUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.GET, "/api/get/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.GET, "/api/get-all/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.POST, "/user/assign-user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/broadcast/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/create-db/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.POST, "/api/add-document/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.PUT, "/api/update/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/delete-db/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/delete-document/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/delete/**").hasAnyRole("ADMIN","USER")
        );
        http.httpBasic(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
    public InMemoryUserDetailsManager addUserToMemory(com.atypon.nosql.node.user.User user) {
        inMemoryUserDetailsManager.createUser(
                User.builder()
                        .username(user.getName())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build()
        );
        return inMemoryUserDetailsManager;
    }
}
