package id.ac.usk.dompetdeadline.model;

import java.util.List;

public record DashboardData(long income, long expense, long activeTaskCost,
                            List<UpcomingTask> upcomingTasks) {
    public long balance() {
        return income - expense;
    }
}
