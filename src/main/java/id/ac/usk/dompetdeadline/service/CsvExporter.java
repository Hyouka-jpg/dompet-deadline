package id.ac.usk.dompetdeadline.service;

import id.ac.usk.dompetdeadline.model.FinanceTransaction;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvExporter {
    private CsvExporter() { }

    public static String quote(String raw) {
        String value = raw == null ? "" : raw;
        // Menghindari formula injection jika CSV dibuka melalui aplikasi spreadsheet.
        if (!value.isEmpty() && (value.charAt(0) == '=' || value.charAt(0) == '+'
                || value.charAt(0) == '-' || value.charAt(0) == '@'
                || value.charAt(0) == '\t' || value.charAt(0) == '\r')) {
            value = "'" + value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public static int export(Path path, List<FinanceTransaction> transactions) throws IOException {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write('\ufeff'); // Membantu Excel membaca UTF-8 dengan benar.
            writer.write("Tanggal,Kategori,Jenis,Jumlah (Rp),Tugas terkait,Catatan\n");
            for (FinanceTransaction transaction : transactions) {
                writer.write(String.join(",",
                        quote(transaction.date().toString()),
                        quote(transaction.categoryName()),
                        quote(transaction.type()),
                        Long.toString(transaction.amount()),
                        quote(transaction.taskTitle()),
                        quote(transaction.note())));
                writer.write('\n');
            }
        }
        return transactions.size();
    }
}
