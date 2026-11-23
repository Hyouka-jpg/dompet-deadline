package id.ac.usk.dompetdeadline.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class DateFormats {
    public static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter INPUT_DEADLINE = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));
    public static final DateTimeFormatter DISPLAY_DEADLINE = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    private DateFormats() { }

    public static LocalDate parseDate(String text) {
        try { return LocalDate.parse(text.strip(), INPUT_DATE); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Tanggal harus berformat HH/BB/TTTT. Contoh: 21/11/2026."); }
    }
    public static LocalDateTime parseDeadline(String text) {
        try { return LocalDateTime.parse(text.strip(), INPUT_DEADLINE); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Tenggat harus berformat HH/BB/TTTT JJ:MM. Contoh: 21/11/2026 16:30."); }
    }
    public static LocalDateTime fromDatabase(String iso) {
        return iso.length() == 10 ? LocalDate.parse(iso).atTime(23, 59) : LocalDateTime.parse(iso);
    }
}
