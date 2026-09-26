package com.la_navaja.backend.infrastructure.config;

import com.la_navaja.backend.domain.models.Role;
import com.la_navaja.backend.infrastructure.repositories.UserJpaRepository;
import com.la_navaja.backend.infrastructure.schemas.UserSchema;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea el usuario ADMIN inicial si no existe ninguno. Se activa solo cuando las variables de
 * entorno INITIAL_ADMIN_* están configuradas.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties adminProperties;

  @Override
  public void run(ApplicationArguments args) {
    if (!adminProperties.isConfigured()) {
      log.info("AdminSeeder: INITIAL_ADMIN_* not set, skipping.");
      return;
    }

    boolean adminExists =
        userJpaRepository.findAll().stream().anyMatch(u -> u.getRole() == Role.ADMIN);

    if (adminExists) {
      log.info("AdminSeeder: ADMIN already exists, skipping.");
      return;
    }

    UserSchema admin =
        UserSchema.builder()
            .firstName(adminProperties.firstName())
            .lastNames(adminProperties.lastNames())
            .email(adminProperties.email().toLowerCase())
            .password(passwordEncoder.encode(adminProperties.password()))
            .role(Role.ADMIN)
            .phone(StringUtils.hasText(adminProperties.phone()) ? adminProperties.phone() : null)
            .build();

    userJpaRepository.save(admin);
    log.info("AdminSeeder: ADMIN created for {}.", adminProperties.email());
  }
}
