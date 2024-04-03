package com.atypon.nosql.bootstrappingnode.security;

import com.atypon.nosql.bootstrappingnode.service.BootstrappingNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class SecurityConfiguration {

    private BootstrappingNodeService bootstrappingNodeService;

    @Autowired
    public SecurityConfiguration(BootstrappingNodeService bootstrappingNodeService) {
        this.bootstrappingNodeService = bootstrappingNodeService;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        List<com.atypon.nosql.bootstrappingnode.entity.User> users = bootstrappingNodeService.getAllUsers();
        List<UserDetails>listOfUser = new ArrayList<>();
        for (com.atypon.nosql.bootstrappingnode.entity.User user : users) {
            UserDetails userDetails = User.builder()
                    .username(user.getName())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
            if(!listOfUser.contains(userDetails)) {
                listOfUser.add(userDetails);
            }
        }
        return new InMemoryUserDetailsManager(listOfUser);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.GET, "/bootstrapping-node/start-cluster").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bootstrapping-node/add-user").hasRole("ADMIN")
        );
        http.httpBasic(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
}