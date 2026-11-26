package id.ac.usk.dompetdeadline.service;

import id.ac.usk.dompetdeadline.model.FinanceTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {
    @TempDir Path directory;
    @Test void escapeQuotesAndCommas() {
        assertEquals("\"Buku \"\"Kuliah\"\", map\"",CsvExporter.quote("Buku \"Kuliah\", map"));
    }
    @Test void protectSpreadsheetFormula() {
        assertEquals("\"'=2+2\"",CsvExporter.quote("=2+2"));
    }
    @Test void exportWithExpectedColumns() throws Exception {
        Path target=directory.resolve("export.csv");
        var entry=new FinanceTransaction(1, LocalDate.of(2026,11,21),25000,3,"Fotokopi",
                "PENGELUARAN",8L,"Laporan PBO","Cetak laporan");
        assertEquals(1,CsvExporter.export(target,List.of(entry)));
        String csv=Files.readString(target);
        assertTrue(csv.contains("Tanggal,Kategori,Jenis,Jumlah (Rp),Tugas terkait,Catatan"));
        assertTrue(csv.contains("25000"));
        assertTrue(csv.contains("Laporan PBO"));
    }
}
