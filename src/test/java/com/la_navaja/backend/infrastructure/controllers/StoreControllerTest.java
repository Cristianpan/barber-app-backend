package com.la_navaja.backend.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.la_navaja.backend.application.dtos.request.DayScheduleRequest;
import com.la_navaja.backend.application.dtos.request.SetStoreScheduleRequest;
import com.la_navaja.backend.application.dtos.response.DayScheduleResponse;
import com.la_navaja.backend.application.dtos.response.SetStoreScheduleResponse;
import com.la_navaja.backend.application.services.StoreService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.BadRequestException;
import com.la_navaja.backend.infrastructure.config.security.AuthenticatedUserProvider;
import com.la_navaja.backend.infrastructure.config.security.CookieAuthFilter;
import com.la_navaja.backend.infrastructure.config.security.JwtService;
import com.la_navaja.backend.infrastructure.config.security.SecurityConfig;
import com.la_navaja.backend.infrastructure.controllers.advice.GlobalExceptionHandler;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@Import({
  SecurityConfig.class,
  CookieAuthFilter.class,
  GlobalExceptionHandler.class,
  AuthenticatedUserProvider.class
})
@TestPropertySource(
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-bytes-long-ok-here",
      "app.jwt.expiration-ms=86400000",
      "app.security.cookie-secure=false"
    })
class StoreControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @MockitoBean private StoreService storeService;
  @MockitoBean private JwtService jwtService;

  private static final SetStoreScheduleRequest VALID_REQUEST =
      new SetStoreScheduleRequest(
          List.of(
              new DayScheduleRequest(
                  DayOfWeek.MONDAY,
                  LocalTime.of(9, 0),
                  LocalTime.of(18, 0),
                  LocalTime.of(13, 0),
                  LocalTime.of(14, 0))));

  @Nested
  class SetScheduleTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSetScheduleSuccessfully() throws Exception {
      SetStoreScheduleResponse response =
          new SetStoreScheduleResponse(
              List.of(
                  new DayScheduleResponse(
                      DayOfWeek.MONDAY,
                      LocalTime.of(9, 0),
                      LocalTime.of(18, 0),
                      LocalTime.of(13, 0),
                      LocalTime.of(14, 0))));

      when(storeService.setSchedule(any(SetStoreScheduleRequest.class))).thenReturn(response);

      mockMvc
          .perform(
              put("/store/schedule")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.days[0].dayOfWeek").value("MONDAY"))
          .andExpect(jsonPath("$.days[0].startTime").value("09:00:00"))
          .andExpect(jsonPath("$.days[0].endTime").value("18:00:00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidRequest() throws Exception {
      mockMvc
          .perform(put("/store/schedule").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectRequest_whenScheduleIsInvalid() throws Exception {
      when(storeService.setSchedule(any(SetStoreScheduleRequest.class)))
          .thenThrow(new BadRequestException(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE));

      mockMvc
          .perform(
              put("/store/schedule")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
      mockMvc
          .perform(
              put("/store/schedule")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(ErrorMessages.UNAUTHORIZED_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldRejectRequestWithoutAdminRole() throws Exception {
      mockMvc
          .perform(
              put("/store/schedule")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(ErrorMessages.FORBIDDEN_MESSAGE));
    }
  }
}
