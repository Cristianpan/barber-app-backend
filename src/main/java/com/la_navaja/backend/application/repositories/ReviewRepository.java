package com.la_navaja.backend.application.repositories;

import com.la_navaja.backend.domain.models.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

  Review save(Review review);

  Optional<Review> findById(Long id);

  List<Review> findAll();

  void deleteById(Long id);

  boolean existsById(Long id);
}
