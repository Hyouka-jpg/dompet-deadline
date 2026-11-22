package id.ac.usk.dompetdeadline.model;

import java.time.LocalDateTime;

public record TaskItem(long id, String title, String course, LocalDateTime deadline,
                       String priority, boolean completed, long totalCost) {
    @Override public String toString() { return title + " · " + course; }
}
