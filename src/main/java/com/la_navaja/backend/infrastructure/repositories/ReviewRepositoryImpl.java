package com.la_navaja.backend.infrastructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.la_navaja.backend.application.repositories.ReviewRepository;
import com.la_navaja.backend.domain.models.Review;
import com.la_navaja.backend.infrastructure.mappers.ReviewMapper;
import com.la_navaja.backend.infrastructure.schemas.ClientSchema;
import com.la_navaja.backend.infrastructure.schemas.ReviewSchema;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;
    private final ReviewMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Review save(Review review) {
        ReviewSchema schema = mapper.toSchema(review);
        schema.setClient(entityManager.getReference(ClientSchema.class, review.clientId()));
        return mapper.toModel(jpaRepository.save(schema));
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Review> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
