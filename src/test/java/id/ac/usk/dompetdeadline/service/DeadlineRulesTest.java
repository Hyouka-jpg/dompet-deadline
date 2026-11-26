package id.ac.usk.dompetdeadline.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineRulesTest {
    private final LocalDateTime now=LocalDateTime.of(2026,11,21,10,0);
    @Test void notifyWhenDueIn24Hours() {
        assertTrue(DeadlineRules.dueWithin24Hours(now.plusHours(5),now,false));
        assertTrue(DeadlineRules.dueWithin24Hours(now.plusHours(24),now,false));
    }
    @Test void dontNotifyCompletedOverdueOrDistantTasks() {
        assertFalse(DeadlineRules.dueWithin24Hours(now.plusHours(5),now,true));
        assertFalse(DeadlineRules.dueWithin24Hours(now.minusMinutes(1),now,false));
        assertFalse(DeadlineRules.dueWithin24Hours(now.plusHours(25),now,false));
    }
}
