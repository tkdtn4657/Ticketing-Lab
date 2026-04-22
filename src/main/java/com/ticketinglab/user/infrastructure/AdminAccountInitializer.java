package com.ticketinglab.user.infrastructure;

import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "docker"})
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sample-data.admin.enabled", havingValue = "true")
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.sample-data.admin.email}")
    private String adminEmail;

    @Value("${app.sample-data.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        String passwordHash = passwordEncoder.encode(adminPassword);
        userRepository.save(User.createAdmin(adminEmail, passwordHash));
    }
}
