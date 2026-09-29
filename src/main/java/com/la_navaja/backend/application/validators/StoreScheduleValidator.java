package com.la_navaja.backend.application.validators;

import com.la_navaja.backend.application.dtos.request.DayScheduleRequest;
import com.la_navaja.backend.application.dtos.request.SetStoreScheduleRequest;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.BadRequestException;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StoreScheduleValidator {

  public void validate(SetStoreScheduleRequest request) {
    Set<DayOfWeek> seenDays = new HashSet<>();

    for (DayScheduleRequest day : request.days()) {
      if (!seenDays.add(day.dayOfWeek())) {
        throw new BadRequestException(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE);
      }

      if (!day.startTime().isBefore(day.endTime())) {
        throw new BadRequestException(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE);
      }

      boolean hasBreakStart = day.breakStart() != null;
      boolean hasBreakEnd = day.breakEnd() != null;

      if (hasBreakStart != hasBreakEnd) {
        throw new BadRequestException(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE);
      }

      if (hasBreakStart) {
        boolean breakOrderInvalid = !day.breakStart().isBefore(day.breakEnd());
        boolean breakOutOfRange =
            day.breakStart().isBefore(day.startTime()) || day.breakEnd().isAfter(day.endTime());

        if (breakOrderInvalid || breakOutOfRange) {
          throw new BadRequestException(ErrorMessages.INVALID_STORE_SCHEDULE_MESSAGE);
        }
      }
    }
  }
}
