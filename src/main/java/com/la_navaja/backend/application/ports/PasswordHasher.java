package com.la_navaja.backend.application.ports;

public interface PasswordHasher {

  boolean matches(String rawPassword, String hashedPassword);

  String hash(String rawPassword);
}
