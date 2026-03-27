// // package com.crowdmonitoring.dashboard.service;

// // import java.time.Instant;
// // import java.time.LocalDate;
// // import java.time.ZoneId;
// // import java.time.ZonedDateTime;
// // import java.time.format.DateTimeFormatter;
// // import java.time.temporal.ChronoUnit;
// // import java.util.ArrayList;
// // import java.util.Comparator;
// // import java.util.List;
// // import java.util.Map;
// // import java.util.Optional;
// // import java.util.function.Function;

// // import org.springframework.stereotype.Service;

// // import com.crowdmonitoring.dashboard.model.DailySummary;
// // import com.crowdmonitoring.dashboard.model.HourlyData;
// // import com.crowdmonitoring.dashboard.model.dto.AnalyticsResponse;
// // import com.crowdmonitoring.dashboard.model.dto.HourlyTrendPointResponse;
// // import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
// // import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;

// // @Service
// // public class AnalyticsEngineService {

// //   private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
// //   private static final DateTimeFormatter HOUR_LABEL = DateTimeFormatter.ofPattern("HH:mm");

// //   public void updateAggregates(long overallTotalCrowd, Instant now,
// //       HourlyDataRepository hourlyRepo,
// //       DailySummaryRepository dailyRepo
// //   ) {
// //     if (overallTotalCrowd < 0) return;

// //     Instant hourStart = startOfHour(now);
// //     LocalDate day = now.atZone(KOLKATA).toLocalDate();

// //     // Hourly update.
// //     HourlyData hourly = hourlyRepo.findByHourStart(hourStart).orElse(null);
// //     if (hourly == null) {
// //       hourly = HourlyData.builder()
// //           .hourStart(hourStart)
// //           .peakCrowd(overallTotalCrowd)
// //           .sumTraffic(overallTotalCrowd)
// //           .countMinutes(1)
// //           .averageTraffic(overallTotalCrowd)
// //           .updatedAt(now)
// //           .build();
// //     } else {
// //       hourly.setPeakCrowd(Math.max(hourly.getPeakCrowd(), overallTotalCrowd));
// //       hourly.setSumTraffic(hourly.getSumTraffic() + overallTotalCrowd);
// //       hourly.setCountMinutes(hourly.getCountMinutes() + 1);
// //       hourly.setAverageTraffic(hourly.getSumTraffic() / hourly.getCountMinutes());
// //       hourly.setUpdatedAt(now);
// //     }
// //     hourlyRepo.save(hourly);

// //     // Daily update.
// //     DailySummary daily = dailyRepo.findByDate(day).orElse(null);
// //     if (daily == null) {
// //       daily = DailySummary.builder()
// //           .date(day)
// //           .peakCrowd(overallTotalCrowd)
// //           .sumTraffic(overallTotalCrowd)
// //           .countMinutes(1)
// //           .averageTraffic(overallTotalCrowd)
// //           .updatedAt(now)
// //           .build();
// //     } else {
// //       daily.setPeakCrowd(Math.max(daily.getPeakCrowd(), overallTotalCrowd));
// //       daily.setSumTraffic(daily.getSumTraffic() + overallTotalCrowd);
// //       daily.setCountMinutes(daily.getCountMinutes() + 1);
// //       daily.setAverageTraffic(daily.getSumTraffic() / daily.getCountMinutes());
// //       daily.setUpdatedAt(now);
// //     }
// //     dailyRepo.save(daily);
// //   }

// //   public AnalyticsResponse buildAnalyticsResponse(HourlyDataRepository hourlyRepo, DailySummaryRepository dailyRepo, Instant now) {
// //     Instant start = startOfHour(now.minus(23, ChronoUnit.HOURS));
// //     Instant end = start.plus(24, ChronoUnit.HOURS);

// //     List<HourlyData> docs = hourlyRepo.findByHourStartBetweenOrderByHourStartAsc(start, end);
// //     Map<Instant, HourlyData> byHour = docs.stream()
// //         .collect(java.util.stream.Collectors.toMap(HourlyData::getHourStart, Function.identity(), (a, b) -> a));

// //     List<HourlyTrendPointResponse> trend = new ArrayList<>();
// //     long peakCrowd = 0;
// //     double sumTraffic = 0;
// //     long countMinutes = 0;

// //     for (int i = 0; i < 24; i++) {
// //       Instant hourStart = start.plus(i, ChronoUnit.HOURS);
// //       HourlyData doc = byHour.get(hourStart);
// //       long value = doc == null ? 0L : Math.round(doc.getAverageTraffic());

// //       // For the chart we use "average crowd for hour" but peak/avg are computed separately.
// //       trend.add(HourlyTrendPointResponse.builder()
// //           .label(formatHourLabel(hourStart))
// //           .value(value)
// //           .build());

// //       if (doc != null) {
// //         peakCrowd = Math.max(peakCrowd, doc.getPeakCrowd());
// //         sumTraffic += doc.getSumTraffic();
// //         countMinutes += doc.getCountMinutes();
// //       }
// //     }

// //     double averageTraffic = countMinutes == 0 ? 0.0 : sumTraffic / countMinutes;

// //     Optional<DailySummary> busiest = dailyRepo.findTopByOrderByAverageTrafficDesc();
// //     String busiestDay = busiest.map(d -> d.getDate().toString()).orElse("N/A");

// //     return AnalyticsResponse.builder()
// //         .hourlyTrend(trend)
// //         .peakCrowd(peakCrowd)
// //         .averageTraffic(averageTraffic)
// //         .busiestDay(busiestDay)
// //         .build();
// //   }

// //   private static Instant startOfHour(Instant instant) {
// //     ZonedDateTime zdt = instant.atZone(KOLKATA).truncatedTo(ChronoUnit.HOURS);
// //     return zdt.toInstant();
// //   }

// //   private static String formatHourLabel(Instant hourStart) {
// //     ZonedDateTime zdt = hourStart.atZone(KOLKATA);
// //     // Label: "HH:00" style (frontend expects label text).
// //     return zdt.format(HOUR_LABEL);
// //   }
// // }

// package com.crowdmonitoring.dashboard.service;

// import java.time.Instant;
// import java.time.LocalDate;
// import java.time.ZoneId;
// import java.time.ZonedDateTime;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.ChronoUnit;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.function.Function;

// import org.springframework.stereotype.Service;

// import com.crowdmonitoring.dashboard.model.DailySummary;
// import com.crowdmonitoring.dashboard.model.HourlyData;
// import com.crowdmonitoring.dashboard.model.dto.AnalyticsResponse;
// import com.crowdmonitoring.dashboard.model.dto.HourlyTrendPointResponse;
// import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
// import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;

// @Service
// public class AnalyticsEngineService {

//   private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
//   private static final DateTimeFormatter HOUR_LABEL = DateTimeFormatter.ofPattern("HH:mm");

//   public void updateAggregates(
//       long overallTotalCrowd,
//       Instant now,
//       HourlyDataRepository hourlyRepo,
//       DailySummaryRepository dailyRepo
//   ) {
//     if (overallTotalCrowd < 0) return;

//     Instant hourStart = startOfHour(now);
//     LocalDate day = now.atZone(KOLKATA).toLocalDate();

//     // Hourly update
//     HourlyData hourly = hourlyRepo.findByHourStart(hourStart).orElse(null);
//     if (hourly == null) {
//       hourly = HourlyData.builder()
//           .hourStart(hourStart)
//           .peakCrowd(overallTotalCrowd)
//           .sumTraffic(overallTotalCrowd)
//           .countMinutes(1)
//           .averageTraffic(overallTotalCrowd)
//           .updatedAt(now)
//           .build();
//     } else {
//       hourly.setPeakCrowd(Math.max(hourly.getPeakCrowd(), overallTotalCrowd));
//       hourly.setSumTraffic(hourly.getSumTraffic() + overallTotalCrowd);
//       hourly.setCountMinutes(hourly.getCountMinutes() + 1);
//       hourly.setAverageTraffic(hourly.getSumTraffic() / hourly.getCountMinutes());
//       hourly.setUpdatedAt(now);
//     }
//     hourlyRepo.save(hourly);

//     // Daily update
//     DailySummary daily = dailyRepo.findByDate(day).orElse(null);
//     if (daily == null) {
//       daily = DailySummary.builder()
//           .date(day)
//           .peakCrowd(overallTotalCrowd)
//           .sumTraffic(overallTotalCrowd)
//           .countMinutes(1)
//           .averageTraffic(overallTotalCrowd)
//           .updatedAt(now)
//           .build();
//     } else {
//       daily.setPeakCrowd(Math.max(daily.getPeakCrowd(), overallTotalCrowd));
//       daily.setSumTraffic(daily.getSumTraffic() + overallTotalCrowd);
//       daily.setCountMinutes(daily.getCountMinutes() + 1);
//       daily.setAverageTraffic(daily.getSumTraffic() / daily.getCountMinutes());
//       daily.setUpdatedAt(now);
//     }
//     dailyRepo.save(daily);
//   }

//   public AnalyticsResponse buildAnalyticsResponse(
//       HourlyDataRepository hourlyRepo,
//       DailySummaryRepository dailyRepo,
//       Instant now
//   ) {
//     Instant start = startOfHour(now.minus(23, ChronoUnit.HOURS));
//     Instant end = start.plus(24, ChronoUnit.HOURS);

//     List<HourlyData> docs = hourlyRepo.findByHourStartBetweenOrderByHourStartAsc(start, end);
//     Map<Instant, HourlyData> byHour = docs.stream()
//         .collect(java.util.stream.Collectors.toMap(
//             HourlyData::getHourStart,
//             Function.identity(),
//             (a, b) -> a
//         ));

//     List<HourlyTrendPointResponse> hourlyTrend = new ArrayList<>();
//     List<HourlyTrendPointResponse> hourlyEntryExit = new ArrayList<>();

//     long peakCrowd = 0;
//     double sumTraffic = 0;
//     long countMinutes = 0;

//     for (int i = 0; i < 24; i++) {
//       Instant hourStart = start.plus(i, ChronoUnit.HOURS);
//       HourlyData doc = byHour.get(hourStart);
//       long value = doc == null ? 0L : Math.round(doc.getAverageTraffic());

//       hourlyTrend.add(HourlyTrendPointResponse.builder()
//           .label(formatHourLabel(hourStart))
//           .value(value)
//           .build());

//       // Minimal backend change: reuse same hourly average values
//       // for the "Hourly Entry & Exit" chart until separate entry/exit
//       // metrics are available.
//       hourlyEntryExit.add(HourlyTrendPointResponse.builder()
//           .label(formatHourLabel(hourStart))
//           .value(value)
//           .build());

//       if (doc != null) {
//         peakCrowd = Math.max(peakCrowd, doc.getPeakCrowd());
//         sumTraffic += doc.getSumTraffic();
//         countMinutes += doc.getCountMinutes();
//       }
//     }

//     double averageTraffic = countMinutes == 0 ? 0.0 : sumTraffic / countMinutes;

//     Optional<DailySummary> busiest = dailyRepo.findTopByOrderByAverageTrafficDesc();
//     String busiestDay = busiest.map(d -> d.getDate().toString()).orElse("N/A");

//     String busiestDayOfWeek = busiest
//         .map(d -> d.getDate().getDayOfWeek().toString())
//         .map(day -> day.substring(0, 1) + day.substring(1).toLowerCase())
//         .orElse("N/A");

//     LocalDate today = now.atZone(KOLKATA).toLocalDate();
//     LocalDate weekStart = today.minusDays(6);

//     List<DailySummary> weeklyDocs = dailyRepo.findAll().stream()
//         .filter(d -> !d.getDate().isBefore(weekStart) && !d.getDate().isAfter(today))
//         .sorted(java.util.Comparator.comparing(DailySummary::getDate))
//         .toList();

//     List<HourlyTrendPointResponse> weeklyCrowdTrend = new ArrayList<>();
//     for (DailySummary d : weeklyDocs) {
//       String label = d.getDate().getDayOfWeek().toString().substring(0, 1)
//           + d.getDate().getDayOfWeek().toString().substring(1, 3).toLowerCase();

//       weeklyCrowdTrend.add(HourlyTrendPointResponse.builder()
//           .label(label)
//           .value(Math.round(d.getAverageTraffic()))
//           .build());
//     }

//     return AnalyticsResponse.builder()
//         // old fields
//         .hourlyTrend(hourlyTrend)
//         .peakCrowd(peakCrowd)
//         .averageTraffic(averageTraffic)
//         .busiestDay(busiestDay)

//         // new fields for updated frontend
//         .peakControl(peakCrowd)
//         .avgDailyTraffic(averageTraffic)
//         .busiestDayOfWeek(busiestDayOfWeek)
//         .hourlyEntryExit(hourlyEntryExit)
//         .weeklyCrowdTrend(weeklyCrowdTrend)
//         .build();
//   }

//   private static Instant startOfHour(Instant instant) {
//     ZonedDateTime zdt = instant.atZone(KOLKATA).truncatedTo(ChronoUnit.HOURS);
//     return zdt.toInstant();
//   }

//   private static String formatHourLabel(Instant hourStart) {
//     ZonedDateTime zdt = hourStart.atZone(KOLKATA);
//     return zdt.format(HOUR_LABEL);
//   }
// }

package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.DailySummary;
import com.crowdmonitoring.dashboard.model.HourlyData;
import com.crowdmonitoring.dashboard.model.dto.AnalyticsResponse;
import com.crowdmonitoring.dashboard.model.dto.HourlyTrendPointResponse;
import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;

@Service
public class AnalyticsEngineService {

  private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
  private static final DateTimeFormatter HOUR_LABEL = DateTimeFormatter.ofPattern("HH:mm");

  public void updateAggregates(
      long overallTotalCrowd,
      Instant now,
      HourlyDataRepository hourlyRepo,
      DailySummaryRepository dailyRepo
  ) {
    if (overallTotalCrowd < 0) return;

    Instant hourStart = startOfHour(now);
    LocalDate day = now.atZone(KOLKATA).toLocalDate();

    // Hourly update
    HourlyData hourly = hourlyRepo.findByHourStart(hourStart).orElse(null);
    if (hourly == null) {
      hourly = HourlyData.builder()
          .hourStart(hourStart)
          .peakCrowd(overallTotalCrowd)
          .sumTraffic(overallTotalCrowd)
          .countMinutes(1)
          .averageTraffic(overallTotalCrowd)
          .updatedAt(now)
          .build();
    } else {
      hourly.setPeakCrowd(Math.max(hourly.getPeakCrowd(), overallTotalCrowd));
      hourly.setSumTraffic(hourly.getSumTraffic() + overallTotalCrowd);
      hourly.setCountMinutes(hourly.getCountMinutes() + 1);
      hourly.setAverageTraffic(hourly.getSumTraffic() / hourly.getCountMinutes());
      hourly.setUpdatedAt(now);
    }
    hourlyRepo.save(hourly);

    // Daily update
    DailySummary daily = dailyRepo.findByDate(day).orElse(null);
    if (daily == null) {
      daily = DailySummary.builder()
          .date(day)
          .peakCrowd(overallTotalCrowd)
          .sumTraffic(overallTotalCrowd)
          .countMinutes(1)
          .averageTraffic(overallTotalCrowd)
          .updatedAt(now)
          .build();
    } else {
      daily.setPeakCrowd(Math.max(daily.getPeakCrowd(), overallTotalCrowd));
      daily.setSumTraffic(daily.getSumTraffic() + overallTotalCrowd);
      daily.setCountMinutes(daily.getCountMinutes() + 1);
      daily.setAverageTraffic(daily.getSumTraffic() / daily.getCountMinutes());
      daily.setUpdatedAt(now);
    }
    dailyRepo.save(daily);
  }

  public AnalyticsResponse buildAnalyticsResponse(
      HourlyDataRepository hourlyRepo,
      DailySummaryRepository dailyRepo,
      Instant now
  ) {
    Instant start = startOfHour(now.minus(23, ChronoUnit.HOURS));
    Instant end = start.plus(24, ChronoUnit.HOURS);

    List<HourlyData> docs = hourlyRepo.findByHourStartBetweenOrderByHourStartAsc(start, end);
    Map<Instant, HourlyData> byHour = docs.stream()
        .collect(java.util.stream.Collectors.toMap(
            HourlyData::getHourStart,
            Function.identity(),
            (a, b) -> a
        ));

    List<HourlyTrendPointResponse> hourlyTrend = new ArrayList<>();
    List<HourlyTrendPointResponse> hourlyEntryExit = new ArrayList<>();

    long peakCrowd = 0;
    double sumTraffic = 0;
    long countMinutes = 0;

    for (int i = 0; i < 24; i++) {
      Instant hourStart = start.plus(i, ChronoUnit.HOURS);
      HourlyData doc = byHour.get(hourStart);
      long value = doc == null ? 0L : Math.round(doc.getAverageTraffic());

      hourlyTrend.add(HourlyTrendPointResponse.builder()
          .label(formatHourLabel(hourStart))
          .value(value)
          .build());

      // Minimal-change approach:
      // Reuse hourly average traffic for the "Hourly Entry & Exit" chart.
      hourlyEntryExit.add(HourlyTrendPointResponse.builder()
          .label(formatHourLabel(hourStart))
          .value(value)
          .build());

      if (doc != null) {
        peakCrowd = Math.max(peakCrowd, doc.getPeakCrowd());
        sumTraffic += doc.getSumTraffic();
        countMinutes += doc.getCountMinutes();
      }
    }

    double averageTraffic = countMinutes == 0 ? 0.0 : sumTraffic / countMinutes;

    Optional<DailySummary> busiest = dailyRepo.findTopByOrderByAverageTrafficDesc();
    String busiestDay = busiest.map(d -> d.getDate().toString()).orElse("N/A");

    String busiestDayOfWeek = busiest
        .map(d -> d.getDate().getDayOfWeek().toString())
        .map(day -> day.substring(0, 1) + day.substring(1).toLowerCase())
        .orElse("N/A");

    LocalDate today = now.atZone(KOLKATA).toLocalDate();
    LocalDate weekStart = today.minusDays(6);

    List<DailySummary> weeklyDocs = dailyRepo.findByDateBetween(weekStart, today);
    weeklyDocs.sort(java.util.Comparator.comparing(DailySummary::getDate));

    List<HourlyTrendPointResponse> weeklyCrowdTrend = new ArrayList<>();
    for (DailySummary d : weeklyDocs) {
      String day = d.getDate().getDayOfWeek().toString();
      String label = day.substring(0, 1) + day.substring(1, 3).toLowerCase();

      weeklyCrowdTrend.add(HourlyTrendPointResponse.builder()
          .label(label)
          .value(Math.round(d.getAverageTraffic()))
          .build());
    }

    return AnalyticsResponse.builder()
        // existing fields
        .hourlyTrend(hourlyTrend)
        .peakCrowd(peakCrowd)
        .averageTraffic(averageTraffic)
        .busiestDay(busiestDay)

        // new fields for updated frontend
        .peakControl(peakCrowd)
        .avgDailyTraffic(averageTraffic)
        .busiestDayOfWeek(busiestDayOfWeek)
        .hourlyEntryExit(hourlyEntryExit)
        .weeklyCrowdTrend(weeklyCrowdTrend)
        .build();
  }

  private static Instant startOfHour(Instant instant) {
    ZonedDateTime zdt = instant.atZone(KOLKATA).truncatedTo(ChronoUnit.HOURS);
    return zdt.toInstant();
  }

  private static String formatHourLabel(Instant hourStart) {
    ZonedDateTime zdt = hourStart.atZone(KOLKATA);
    return zdt.format(HOUR_LABEL);
  }
}