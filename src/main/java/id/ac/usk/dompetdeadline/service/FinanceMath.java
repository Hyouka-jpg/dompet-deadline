package id.ac.usk.dompetdeadline.service;

public final class FinanceMath {
    private FinanceMath() { }
    public static long balance(long income, long expense) {
        return Math.subtractExact(income, expense);
    }
    public static void requirePositiveAmount(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Nominal harus lebih besar dari Rp 0.");
    }
}
