package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.SetStoreInfoRequest;
import com.la_navaja.backend.application.dtos.response.SetStoreInfoResponse;
import com.la_navaja.backend.application.repositories.StoreInfoRepository;
import com.la_navaja.backend.domain.models.StoreInfo;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreInfoServiceTest {

  @Mock private StoreInfoRepository storeInfoRepository;

  @InjectMocks private StoreInfoService storeInfoService;

  @Nested
  class SetStoreInfoTests {

    @Test
    void shouldSetStoreInfoSuccessfully_whenNoPreviousInfoExists() {
      SetStoreInfoRequest request =
          new SetStoreInfoRequest(
              "La Navaja",
              "Calle 1",
              "555-0100",
              "info@lanavaja.com",
              "Historia",
              "Sobre nosotros");

      StoreInfo saved =
          StoreInfo.builder()
              .id(1L)
              .name("La Navaja")
              .address("Calle 1")
              .phone("555-0100")
              .email("info@lanavaja.com")
              .history("Historia")
              .aboutUs("Sobre nosotros")
              .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
              .build();

      when(storeInfoRepository.findAll()).thenReturn(List.of());
      when(storeInfoRepository.save(any(StoreInfo.class))).thenReturn(saved);

      SetStoreInfoResponse response = storeInfoService.setStoreInfo(request);

      assertThat(response.id()).isEqualTo(1L);
      assertThat(response.name()).isEqualTo("La Navaja");
      assertThat(response.address()).isEqualTo("Calle 1");
      assertThat(response.phone()).isEqualTo("555-0100");
      assertThat(response.email()).isEqualTo("info@lanavaja.com");
      assertThat(response.history()).isEqualTo("Historia");
      assertThat(response.aboutUs()).isEqualTo("Sobre nosotros");

      ArgumentCaptor<StoreInfo> captor = ArgumentCaptor.forClass(StoreInfo.class);
      verify(storeInfoRepository).save(captor.capture());
      assertThat(captor.getValue().id()).isNull();
    }

    @Test
    void shouldUpdateExistingStoreInfo_whenInfoAlreadyExists() {
      SetStoreInfoRequest request =
          new SetStoreInfoRequest(
              "La Navaja actualizada",
              "Calle 2",
              "555-0200",
              "nuevo@lanavaja.com",
              "Historia actualizada",
              "Sobre nosotros actualizado");

      StoreInfo existing = StoreInfo.builder().id(5L).name("La Navaja").build();

      StoreInfo saved =
          StoreInfo.builder()
              .id(5L)
              .name("La Navaja actualizada")
              .address("Calle 2")
              .phone("555-0200")
              .email("nuevo@lanavaja.com")
              .history("Historia actualizada")
              .aboutUs("Sobre nosotros actualizado")
              .updatedAt(LocalDateTime.of(2026, 2, 1, 10, 0))
              .build();

      when(storeInfoRepository.findAll()).thenReturn(List.of(existing));
      when(storeInfoRepository.save(any(StoreInfo.class))).thenReturn(saved);

      SetStoreInfoResponse response = storeInfoService.setStoreInfo(request);

      assertThat(response.id()).isEqualTo(5L);
      assertThat(response.name()).isEqualTo("La Navaja actualizada");

      ArgumentCaptor<StoreInfo> captor = ArgumentCaptor.forClass(StoreInfo.class);
      verify(storeInfoRepository).save(captor.capture());
      assertThat(captor.getValue().id()).isEqualTo(5L);
      assertThat(captor.getValue().name()).isEqualTo("La Navaja actualizada");
    }
  }
}
