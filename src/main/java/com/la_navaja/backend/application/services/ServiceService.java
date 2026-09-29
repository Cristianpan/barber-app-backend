package com.la_navaja.backend.application.services;

import com.la_navaja.backend.application.dtos.request.CreateServiceRequest;
import com.la_navaja.backend.application.dtos.response.CreateServiceResponse;
import com.la_navaja.backend.application.repositories.OfferingRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.ResourceAlreadyExistsException;
import com.la_navaja.backend.domain.models.Offering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceService {

  private final OfferingRepository offeringRepository;

  @Transactional
  public CreateServiceResponse createService(CreateServiceRequest request) {
    String normalizedName = request.name().trim().toLowerCase();

    if (offeringRepository.findByNormalizedName(normalizedName).isPresent()) {
      throw new ResourceAlreadyExistsException(ErrorMessages.SERVICE_NAME_ALREADY_EXISTS_MESSAGE);
    }

    Offering offering =
        offeringRepository.save(
            Offering.builder()
                .name(request.name())
                .normalizedName(normalizedName)
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .price(request.price())
                .enabled(true)
                .build());

    return new CreateServiceResponse(
        offering.id(),
        offering.name(),
        offering.description(),
        offering.durationMinutes(),
        offering.price(),
        offering.enabled(),
        offering.createdAt());
  }
}
