package thantz.trelloello;

import net.time4j.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.Assert;

import java.time.LocalTime;
import java.util.stream.Stream;

public class RepetitionScheduleTests {
    private static Stream<Arguments> repetitionScheduleParams() {
        int secsInDay = 60*60*24;

        return Stream.of(
                Arguments.of("", false, 0, 0, 0, 0, 0, 0, 0, null, false),
                Arguments.of("dsadsadsa", false, 0, 0, 0, 0, 0, 0, 0, null, false),
                Arguments.of("P1D", false, 0, 0, 0, 0, 0, 0, 0, null, false),
                Arguments.of("bsjadbjhsad{P1D}dnjsnfjdfs", true, 0, 0, 0, 1, 0, 0, 0, null, false),
                Arguments.of("bsjadbjhsad{P1D}", true, 0, 0, 0, 1, 0, 0, 0, null, false),
                Arguments.of("{P1D}dnjsnfjdfs", true, 0, 0, 0, 1, 0, 0, 0, null, false),
                Arguments.of("{P1Y}", true, 1, 0, 0, 0, 0, 0, 0, null, false),
                Arguments.of("{P1M}", true, 0, 1, 0, 0, 0, 0, 0, null, false),
                Arguments.of("{P2W}", true, 0, 0, 2, 0, 0, 0, 0, null, false),
                Arguments.of("{P3W2D}", true, 0, 0, 3, 2, 0, 0, 0, null, false),
                Arguments.of("{P1Y6M}", true, 1, 6, 0, 0, 0, 0, 0, null, false),
                Arguments.of("{P18M}", true, 0, 18, 0, 0, 0, 0, 0, null, false),
                Arguments.of("{PT10M}", true, 0, 0, 0, 0, 0, 10, 0, null, false),
                Arguments.of("{P1DT12H}", true, 0, 0, 0, 1, 12, 0, 0, null, false),
                Arguments.of("{PT36H}", true, 0, 0, 0, 0, 36, 0, 0, null, false),
                Arguments.of("{PT50M}", true, 0, 0, 0, 0, 0, 50, 0, null, false),
                Arguments.of("{PT90S}", true, 0, 0, 0, 0, 0, 0, 90, null, false),
                Arguments.of("{P3D 18:00}", true, 0, 0, 0, 3, 0, 0, 0, LocalTime.of(18, 0, 0), false),
                Arguments.of("{P1WT1M 05:21}", true, 0, 0, 1, 0, 0, 1, 0, LocalTime.of(5, 21, 0), false),
                Arguments.of("{20:00}", false, 0, 0, 0, 0, 0, 0, 0, null, false),
                Arguments.of("{P18M start}", true, 0, 18, 0, 0, 0, 0, 0, null, true),
                Arguments.of("{PT10M start}", true, 0, 0, 0, 0, 0, 10, 0, null, true),
                Arguments.of("{P1DT12H start}", true, 0, 0, 0, 1, 12, 0, 0, null, true),
                Arguments.of("{P3D 18:00 start}", true, 0, 0, 0, 3, 0, 0, 0, LocalTime.of(18, 0, 0), true)
        );
    }

    @ParameterizedTest
    @MethodSource("repetitionScheduleParams")
    void checkRepetitionSchedules(String cardDescription,
                                  boolean shouldFindSchedule,
                                  int years,
                                  int months,
                                  int weeks,
                                  int days,
                                  int hours,
                                  int mins,
                                  int secs,
                                  LocalTime startTime,
                                  boolean fromStart) {
        RepetitionSchedule schedule = RepetitionSchedule.fromCardDescription(cardDescription);

        if (!shouldFindSchedule) {
            Assert.isNull(schedule,
                    String.format("%s unexpectedly parsed as valid schedule", cardDescription));
            return;
        }

        Assert.isTrue(schedule != null,
                String.format("Schedule not found in %s", cardDescription));

        Assert.isTrue(schedule.getDuration().getPartialAmount(CalendarUnit.YEARS) == years
                        && schedule.getDuration().getPartialAmount(CalendarUnit.MONTHS) == months
                        && schedule.getDuration().getPartialAmount(CalendarUnit.WEEKS) == weeks
                        && schedule.getDuration().getPartialAmount(CalendarUnit.DAYS) == days
                        && schedule.getDuration().getPartialAmount(ClockUnit.HOURS) == hours
                        && schedule.getDuration().getPartialAmount(ClockUnit.MINUTES) == mins
                        && schedule.getDuration().getPartialAmount(ClockUnit.SECONDS) == secs,
                String.format("%s duration unexpectedly parsed as %s",
                        cardDescription, schedule.getDuration()));

        if (startTime == null) {
            Assert.isNull(schedule.getStartTime(),
                    String.format("Time unexpectedly found in %s", cardDescription));
        } else {
            Assert.isTrue(schedule.getStartTime().equals(startTime),
                    String.format("%s time unexpectedly parsed as %s",
                            cardDescription, schedule.getStartTime()));
        }

        if (fromStart) {
            Assert.isTrue(schedule.getRepeatFromStartDate(),
                    String.format("Schedule %s wasn't found to be 'from start'",
                            cardDescription));
        } else {
            Assert.isTrue(!schedule.getRepeatFromStartDate(),
                    String.format("Schedule %s was incorrectly found to be 'from start'",
                            cardDescription));
        }

    }
}
