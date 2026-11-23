package id.ac.usk.dompetdeadline.service;

import id.ac.usk.dompetdeadline.model.DashboardData;
import id.ac.usk.dompetdeadline.repository.DashboardRepository;

import java.sql.SQLException;
import java.time.YearMonth;

public final class DashboardService {
    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public DashboardData getDashboard(YearMonth month) throws SQLException {
        return new DashboardData(
                repository.sumMonthly(month, "PEMASUKAN"),
                repository.sumMonthly(month, "PENGELUARAN"),
                repository.sumActiveTaskExpenses(),
                repository.findThreeUnfinishedTasks());
    }
}
