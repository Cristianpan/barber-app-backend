package com.la_navaja.backend.application.services;

import com.la_navaja.backend.application.dtos.request.DayScheduleRequest;
import com.la_navaja.backend.application.dtos.request.SetStoreScheduleRequest;
import com.la_navaja.backend.application.dtos.response.DayScheduleResponse;
import com.la_navaja.backend.application.dtos.response.SetStoreScheduleResponse;
import com.la_navaja.backend.application.repositories.BusinessHourRepository;
import com.la_navaja.backend.application.validators.StoreScheduleValidator;
import com.la_navaja.backend.domain.models.BusinessHour;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

  private final BusinessHourRepository businessHourRepository;
  private final StoreScheduleValidator storeScheduleValidator;

  @Transactional
  public SetStoreScheduleResponse setSchedule(SetStoreScheduleRequest request) {
    storeScheduleValidator.validate(request);

    businessHourRepository.deleteAll();

    Map<DayOfWeek, List<BusinessHour>> savedByDay = new EnumMap<>(DayOfWeek.class);

    for (DayScheduleRequest day : request.days()) {
      List<BusinessHour> savedIntervals = new ArrayList<>();

      if (day.breakStart() != null) {
        savedIntervals.add(
            businessHourRepository.save(
                BusinessHour.builder()
                    .dayOfWeek(day.dayOfWeek())
                    .startTime(day.startTime())
                    .endTime(day.breakStart())
                    .build()));
        savedIntervals.add(
            businessHourRepository.save(
                BusinessHour.builder()
                    .dayOfWeek(day.dayOfWeek())
                    .startTime(day.breakEnd())
                    .endTime(day.endTime())
                    .build()));
      } else {
        savedIntervals.add(
            businessHourRepository.save(
                BusinessHour.builder()
                    .dayOfWeek(day.dayOfWeek())
                    .startTime(day.startTime())
                    .endTime(day.endTime())
                    .build()));
      }

      savedByDay.put(day.dayOfWeek(), savedIntervals);
    }

    List<DayScheduleResponse> responseDays =
        savedByDay.entrySet().stream()
            .map(entry -> toDayScheduleResponse(entry.getKey(), entry.getValue()))
            .toList();

    return new SetStoreScheduleResponse(responseDays);
  }

  private DayScheduleResponse toDayScheduleResponse(
      DayOfWeek dayOfWeek, List<BusinessHour> intervals) {
    List<BusinessHour> sorted =
        intervals.stream().sorted(Comparator.comparing(BusinessHour::startTime)).toList();

    if (sorted.size() == 1) {
      BusinessHour single = sorted.get(0);
      return new DayScheduleResponse(dayOfWeek, single.startTime(), single.endTime(), null, null);
    }

    BusinessHour first = sorted.get(0);
    BusinessHour second = sorted.get(1);
    return new DayScheduleResponse(
        dayOfWeek, first.startTime(), second.endTime(), first.endTime(), second.startTime());
  }
}
