package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.CreateServiceRequest;
import com.la_navaja.backend.application.dtos.response.CreateServiceResponse;
import com.la_navaja.backend.application.repositories.OfferingRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.ResourceAlreadyExistsException;
import com.la_navaja.backend.domain.models.Offering;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

  @Mock private OfferingRepository offeringRepository;

  @InjectMocks private ServiceService serviceService;

  @Nested
  class CreateServiceTests {

    @Test
    void shouldCreateServiceSuccessfully() {
      CreateServiceRequest request =
          new CreateServiceRequest("Corte", "Corte de cabello", 30, BigDecimal.valueOf(100));

      Offering saved =
          Offering.builder()
              .id(1L)
              .name("Corte")
              .normalizedName("corte")
              .description("Corte de cabello")
              .durationMinutes(30)
              .price(BigDecimal.valueOf(100))
              .enabled(true)
              .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
              .build();

      when(offeringRepository.findByNormalizedName("corte")).thenReturn(Optional.empty());
      when(offeringRepository.save(any(Offering.class))).thenReturn(saved);

      CreateServiceResponse response = serviceService.createService(request);

      assertThat(response.id()).isEqualTo(1L);
      assertThat(response.name()).isEqualTo("Corte");
      assertThat(response.description()).isEqualTo("Corte de cabello");
      assertThat(response.durationMinutes()).isEqualTo(30);
      assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(100));
      assertThat(response.enabled()).isTrue();

      ArgumentCaptor<Offering> captor = ArgumentCaptor.forClass(Offering.class);
      verify(offeringRepository).save(captor.capture());
      Offering toSave = captor.getValue();
      assertThat(toSave.normalizedName()).isEqualTo("corte");
      assertThat(toSave.enabled()).isTrue();
    }

    @Test
    void shouldRejectCreation_whenNameAlreadyExists() {
      CreateServiceRequest request =
          new CreateServiceRequest("Corte", "Corte de cabello", 30, BigDecimal.valueOf(100));

      when(offeringRepository.findByNormalizedName("corte"))
          .thenReturn(Optional.of(Offering.builder().id(2L).normalizedName("corte").build()));

      assertThatThrownBy(() -> serviceService.createService(request))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .hasMessage(ErrorMessages.SERVICE_NAME_ALREADY_EXISTS_MESSAGE);
    }
  }
}
