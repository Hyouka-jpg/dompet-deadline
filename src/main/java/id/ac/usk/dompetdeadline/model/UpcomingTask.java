package id.ac.usk.dompetdeadline.model;

import java.time.LocalDate;

public record UpcomingTask(long id, String title, String course, LocalDate deadline, String priority) { }
