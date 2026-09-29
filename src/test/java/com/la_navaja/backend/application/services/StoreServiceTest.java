package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.DayScheduleRequest;
import com.la_navaja.backend.application.dtos.request.SetStoreScheduleRequest;
import com.la_navaja.backend.application.dtos.response.DayScheduleResponse;
import com.la_navaja.backend.application.dtos.response.SetStoreScheduleResponse;
import com.la_navaja.backend.application.repositories.BusinessHourRepository;
import com.la_navaja.backend.application.validators.StoreScheduleValidator;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.BadRequestException;
import com.la_navaja.backend.domain.models.BusinessHour;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

  @Mock private BusinessHourRepository businessHourRepository;

  private StoreService storeService;

  @BeforeEach
  void setUp() {
    storeService = new StoreService(businessHourRepository, new StoreScheduleValidator());
  }

  @Nested
  class SetScheduleTests {

    @Test
    void shouldSetScheduleSuccessfully_withDifferentHoursAndOptionalBreakPerDay() {
      DayScheduleRequest monday =
          new DayScheduleRequest(
              DayOfWeek.MONDAY,
              LocalTime.of(9, 0),
              LocalTime.of(18, 0),
              LocalTime.of(13, 0),
              LocalTime.of(14, 0));
      DayScheduleRequest tuesday =
          new DayScheduleRequest(
              DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0), null, null);
      SetStoreScheduleRequest request = new SetStoreScheduleRequest(List.of(monday, tuesday));

      when(businessHourRepository.save(any(BusinessHour.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      SetStoreScheduleResponse response = storeService.setSchedule(request);

      verify(businessHourRepository).deleteAll();

      ArgumentCaptor<BusinessHour> captor = ArgumentCaptor.forClass(BusinessHour.class);
      verify(businessHourRepository, times(3)).save(captor.capture());
      assertThat(captor.getAllValues())
          .extracting(BusinessHour::dayOfWeek, BusinessHour::startTime, BusinessHour::endTime)
          .containsExactlyInAnyOrder(
              Tuple.tuple(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)),
              Tuple.tuple(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0)),
              Tuple.tuple(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0)));

      assertThat(response.days())
          .extracting(
              DayScheduleResponse::dayOfWeek,
              DayScheduleResponse::startTime,
              DayScheduleResponse::endTime,
              DayScheduleResponse::breakStart,
              DayScheduleResponse::breakEnd)
          .containsExactlyInAnyOrder(
              Tuple.tuple(
                  DayOfWeek.MONDAY,
                  LocalTime.of(9, 0),
                  LocalTime.of(18, 0),
                  LocalTime.of(13, 0),
                  LocalTime.of(14, 0)),
              Tuple.tuple(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0), null, null));
    }

    @ParameterizedTest
    @MethodSource(
        "com.la_navaja.backend.application.services.StoreServiceTest#invalidScheduleRequests")
    void shouldRejectSchedule_whenScheduleIsInvalid(SetStoreScheduleRequest request) {
      assertThatThrownBy(() -> storeService.setSchedule(request))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE);
    }
  }

  static Stream<Arguments> invalidScheduleRequests() {
    DayScheduleRequest repeatedFirst =
        new DayScheduleRequest(
            DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);
    DayScheduleRequest repeatedDuplicate =
        new DayScheduleRequest(
            DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(15, 0), null, null);

    DayScheduleRequest invalidRange =
        new DayScheduleRequest(
            DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(9, 0), null, null);

    DayScheduleRequest incompleteBreak =
        new DayScheduleRequest(
            DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(13, 0), null);

    DayScheduleRequest breakOutsideRange =
        new DayScheduleRequest(
            DayOfWeek.MONDAY,
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
            LocalTime.of(8, 0),
            LocalTime.of(14, 0));

    return Stream.of(
        Arguments.of(new SetStoreScheduleRequest(List.of(repeatedFirst, repeatedDuplicate))),
        Arguments.of(new SetStoreScheduleRequest(List.of(invalidRange))),
        Arguments.of(new SetStoreScheduleRequest(List.of(incompleteBreak))),
        Arguments.of(new SetStoreScheduleRequest(List.of(breakOutsideRange))));
  }
}
