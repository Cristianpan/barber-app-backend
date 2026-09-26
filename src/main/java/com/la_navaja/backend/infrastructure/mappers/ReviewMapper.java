package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.Review;
import com.la_navaja.backend.infrastructure.schemas.ReviewSchema;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

  public Review toModel(ReviewSchema schema) {
    return Review.builder()
        .id(schema.getId())
        .clientId(schema.getClient().getId())
        .rating(schema.getRating())
        .comment(schema.getComment())
        .status(schema.getStatus())
        .createdAt(schema.getCreatedAt())
        .updatedAt(schema.getUpdatedAt())
        .build();
  }

  /**
   * Solo campos escalares: la referencia al cliente la pone el repositorio con {@code
   * getReference}.
   */
  public ReviewSchema toSchema(Review model) {
    return ReviewSchema.builder()
        .id(model.id())
        .rating(model.rating())
        .comment(model.comment())
        .status(model.status())
        .build();
  }
}
