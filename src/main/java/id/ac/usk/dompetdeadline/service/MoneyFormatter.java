package id.ac.usk.dompetdeadline.service;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {
    private MoneyFormatter() { }

    public static String rupiah(long amount) {
        return "Rp " + NumberFormat.getIntegerInstance(new Locale("id", "ID")).format(amount);
    }
}
