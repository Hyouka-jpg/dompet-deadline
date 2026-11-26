package id.ac.usk.dompetdeadline.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FinanceMathTest {
    @Test void balanceSubtractsIncomeAndExpenses() {
        assertEquals(250_000L, FinanceMath.balance(500_000L,250_000L));
    }
    @Test void negativeBalanceIsAllowed() {
        assertEquals(-75_000L, FinanceMath.balance(25_000L,100_000L));
    }
    @Test void amountMustBePositive() {
        assertThrows(IllegalArgumentException.class,()->FinanceMath.requirePositiveAmount(0));
        assertThrows(IllegalArgumentException.class,()->FinanceMath.requirePositiveAmount(-50));
        assertDoesNotThrow(()->FinanceMath.requirePositiveAmount(50_000));
    }
}
