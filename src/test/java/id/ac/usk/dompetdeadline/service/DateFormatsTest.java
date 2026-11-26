package id.ac.usk.dompetdeadline.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DateFormatsTest {
    @Test void validDateInput() {
        assertEquals(LocalDate.of(2026,11,21),DateFormats.parseDate("21/11/2026"));
    }
    @Test void invalidDateIsExplained() {
        assertThrows(IllegalArgumentException.class,()->DateFormats.parseDate("31/02/2026"));
    }
    @Test void deadlineHasDateAndTime() {
        assertEquals(LocalDateTime.of(2026,11,21,16,30),DateFormats.parseDeadline("21/11/2026 16:30"));
    }
}
