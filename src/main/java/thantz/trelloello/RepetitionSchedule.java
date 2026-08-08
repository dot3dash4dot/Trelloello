package thantz.trelloello;

import com.julienvey.trello.domain.Card;
import net.time4j.Duration;
import net.time4j.IsoUnit;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepetitionSchedule {
    private final String sourceText;
    private final Duration<IsoUnit> duration;
    private final LocalTime startTime;
    private final boolean fromStart;

    public RepetitionSchedule(String sourceText, Duration<IsoUnit> duration, LocalTime startTime, boolean fromStart) {
        this.sourceText = sourceText;
        this.duration = duration;
        this.startTime = startTime;
        this.fromStart = fromStart;
    }

    public String getSourceText() {
        return sourceText;
    }

    public Duration<IsoUnit> getDuration() {
        return duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public boolean getFromStart() { return fromStart; }

    public static RepetitionSchedule fromCardDescription(String description) {
        //The string we're looking for is an ISO 8601 duration, followed by an optional time and/or 'start' label, e.g:
        //	{P3D 18:00}
        //	{P1WT1M 5:21}
        //	{P1Y}
        //	{P2W 18:00 start}
        //	{P1Y start}
        String regexPattern = "\\{(P\\S{2,})(?:\\s(\\d{1,2}:\\d{2}))?( start)?}";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String durationString = matcher.group(1);
            Duration<IsoUnit> duration;
            try {
                duration = Duration.parsePeriod(durationString);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Can't parse duration %s", durationString));
            }

            LocalTime startTime = null;
            String timeString = matcher.group(2);
            if (timeString != null) { //We have a time portion as well
                try {
                    startTime = LocalTime.parse(timeString);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Can't parse time %s", timeString));
                }
            }

            boolean fromStart = matcher.group(3) != null;

            return new RepetitionSchedule(matcher.group(), duration, startTime, fromStart);
        } else {
            return null;
        }
    }
}
