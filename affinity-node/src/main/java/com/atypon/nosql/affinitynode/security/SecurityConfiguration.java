package com.atypon.nosql.affinitynode.security;


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
                        .password("{noop}admin")
                        .roles("ADMIN")
                        .build()
        );
        return inMemoryUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.POST, "/user/assign-user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("USER")
        );
        http.httpBasic(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }

    public InMemoryUserDetailsManager addUserToMemory(com.atypon.nosql.affinitynode.user.User user) {
        inMemoryUserDetailsManager.createUser(
                User.builder()
                        .username(user.getName())
                        .password(user.getPassword())
                        .roles(user.getPassword())
                        .build()
        );
        return inMemoryUserDetailsManager;
    }
}
