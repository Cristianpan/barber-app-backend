package com.la_navaja.backend.application.repositories;

import com.la_navaja.backend.domain.models.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

  User save(User user);

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  List<User> findAll();

  void deleteById(Long id);

  boolean existsById(Long id);
}
