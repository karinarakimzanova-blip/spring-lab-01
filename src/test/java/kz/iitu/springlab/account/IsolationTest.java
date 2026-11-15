package kz.iitu.springlab.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IsolationTest {

    @Autowired PlatformTransactionManager txManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountRepository accounts;

    private Long aliceId;

    record Reads(BigDecimal first, BigDecimal second) { }

    @BeforeEach
    void setUp() {
        accounts.deleteAll();
        aliceId = accounts.save(new Account("Alice", new BigDecimal("1000.00"))).getId();
    }

    private BigDecimal readBalance() {
        return jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, aliceId);
    }

    /** Читатель читает дважды; между чтениями другая транзакция меняет баланс и фиксирует. */
    private Reads readTwiceWithConcurrentUpdate(int isolationLevel) {
        TransactionTemplate reader = new TransactionTemplate(txManager);
        reader.setIsolationLevel(isolationLevel);

        return reader.execute(status -> {
            BigDecimal first = readBalance();

            TransactionTemplate writer = new TransactionTemplate(txManager);
            writer.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            writer.executeWithoutResult(s ->
                    jdbc.update("UPDATE accounts SET balance = balance - 100 WHERE id = ?", aliceId));

            BigDecimal second = readBalance();
            return new Reads(first, second);
        });
    }

    @Test
    void readCommitted_seesChangeOnSecondRead() {
        Reads r = readTwiceWithConcurrentUpdate(TransactionDefinition.ISOLATION_READ_COMMITTED);
        System.out.println("READ_COMMITTED:  first=" + r.first() + ", second=" + r.second());

        assertThat(r.first()).isEqualByComparingTo("1000.00");
        assertThat(r.second()).isEqualByComparingTo("900.00");   // неповторяющееся чтение
    }

    @Test
    void repeatableRead_observedBehaviour() {
        Reads r = readTwiceWithConcurrentUpdate(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        System.out.println("REPEATABLE_READ: first=" + r.first() + ", second=" + r.second());

        assertThat(r.first()).isEqualByComparingTo("1000.00");
        // Второе значение мы не проверяем жёстко: нужно записать фактическое поведение H2.
        assertThat(r.second()).isNotNull();
    }
}
