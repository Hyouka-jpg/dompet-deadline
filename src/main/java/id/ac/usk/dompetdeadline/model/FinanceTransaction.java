package id.ac.usk.dompetdeadline.model;

import java.time.LocalDate;

public record FinanceTransaction(long id, LocalDate date, long amount, long categoryId,
                                 String categoryName, String type, Long taskId,
                                 String taskTitle, String note) { }
