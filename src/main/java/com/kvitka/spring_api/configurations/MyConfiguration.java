package com.kvitka.spring_api.configurations;

import com.kvitka.spring_api.entities.Role;
import com.kvitka.spring_api.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyConfiguration {

    private final RoleRepository roleRepository;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        Role roleUser = roleRepository.findByName("ROLE_USER");
        if (roleUser == null) {
            Role role = new Role();
            role.setName("ROLE_USER");
            roleRepository.save(role);
            log.warn("ROLE_USER was not found, now successfully created");
        } else {
            log.info("ROLE_USER exists, no actions needed");
        }
    }
}
