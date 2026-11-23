package id.ac.usk.dompetdeadline.service;

import java.time.LocalDateTime;

public final class DeadlineRules {
    private DeadlineRules() { }
    public static boolean dueWithin24Hours(LocalDateTime deadline, LocalDateTime now, boolean completed) {
        return !completed && !deadline.isBefore(now) && !deadline.isAfter(now.plusHours(24));
    }
}
