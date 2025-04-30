package thantz.trelloello;

import com.julienvey.trello.domain.Card;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;

public class Helpers {
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public static LocalDate dateToLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    public static boolean pastCardStartDate(Card card)
    {
        LocalDateTime startDateTime = getCardStartDateTime(card);
        return startDateTime == null || LocalDateTime.now().isAfter(startDateTime);
    }

    public static LocalDateTime getCardStartDateTime(Card card) {
        return getCardStartDateTime(card, null);
    }

    public static LocalDateTime getCardStartDateTime(Card card, LocalDateTime valueInsteadOfNull) {
        if (card.getStart() == null) {
            return valueInsteadOfNull;
        } else {
            //Trello doesn't officially support start TIMES, only dates. Cards' start dates seem to
            //include a time, though we can't be sure what that time will be. Therefore, strip out
            //the time here
            LocalDate startDate = dateToLocalDate(card.getStart());

            //For start time we instead use the value from the card's title
            LocalTime time = CardNameWithTime.getTime(card.getName());
            if (time == null) {
                time = LocalTime.MIN;
            }

            return startDate.atTime(time);
        }
    }

    public static String PrettyFormat(Temporal temporal) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.systemDefault());

        return formatter.format(temporal);
    }
}
