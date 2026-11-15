package kz.iitu.springlab.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    AccountRepository accounts;

    private Long aliceId;

    @BeforeEach
    void setUp() {
        accounts.deleteAll();
        aliceId = accounts.save(new Account("Alice", new BigDecimal("1000.00"))).getId();
    }

    @Test
    void secondStaleUpdateIsRejected() {
        // два "пользователя" загрузили один и тот же счёт
        Account copy1 = accounts.findById(aliceId).orElseThrow();
        Account copy2 = accounts.findById(aliceId).orElseThrow();
        System.out.println("Loaded: copy1.version=" + copy1.getVersion()
                + ", copy2.version=" + copy2.getVersion());

        // первый сохраняет: версия в базе становится 1
        copy1.debit(new BigDecimal("100.00"));
        accounts.saveAndFlush(copy1);

        // второй пытается сохранить устаревшую копию (версия 0)
        copy2.debit(new BigDecimal("200.00"));
        assertThatThrownBy(() -> accounts.saveAndFlush(copy2))
                .isInstanceOf(OptimisticLockingFailureException.class);

        Account result = accounts.findById(aliceId).orElseThrow();
        System.out.println("After conflict: balance=" + result.getBalance()
                + ", version=" + result.getVersion());

        assertThat(result.getBalance()).isEqualByComparingTo("900.00");  // применилось только первое
        assertThat(result.getVersion()).isEqualTo(1L);
    }

    @Test
    void retryAfterReloadSucceeds() {
        Account copy1 = accounts.findById(aliceId).orElseThrow();
        Account copy2 = accounts.findById(aliceId).orElseThrow();

        copy1.debit(new BigDecimal("100.00"));
        accounts.saveAndFlush(copy1);

        copy2.debit(new BigDecimal("200.00"));
        assertThatThrownBy(() -> accounts.saveAndFlush(copy2))
                .isInstanceOf(OptimisticLockingFailureException.class);

        // повтор: перечитать актуальное состояние и применить операцию заново
        Account fresh = accounts.findById(aliceId).orElseThrow();
        fresh.debit(new BigDecimal("200.00"));
        accounts.saveAndFlush(fresh);

        Account result = accounts.findById(aliceId).orElseThrow();
        assertThat(result.getBalance()).isEqualByComparingTo("700.00");
        assertThat(result.getVersion()).isEqualTo(2L);
    }
}
