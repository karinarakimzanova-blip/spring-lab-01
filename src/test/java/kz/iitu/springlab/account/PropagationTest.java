package kz.iitu.springlab.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PropagationTest {

    @Autowired PropagationDemoService service;
    @Autowired AccountRepository accounts;
    @Autowired AuditRepository audit;

    private Long aliceId;

    @BeforeEach
    void setUp() {
        audit.deleteAll();
        accounts.deleteAll();
        aliceId = accounts.save(new Account("Alice", new BigDecimal("1000.00"))).getId();
    }

    private BigDecimal balance() {
        return accounts.findById(aliceId).orElseThrow().getBalance();
    }

    @Test
    void requiredSuccess() {
        service.withdrawRequired(aliceId, new BigDecimal("100.00"), false);

        assertThat(balance()).isEqualByComparingTo("900.00");
        assertThat(audit.count()).isEqualTo(1);
    }

    @Test
    void requiredOuterFails_auditIsRolledBackToo() {
        assertThatThrownBy(() -> service.withdrawRequired(aliceId, new BigDecimal("100.00"), true))
                .isInstanceOf(IllegalStateException.class);

        assertThat(balance()).isEqualByComparingTo("1000.00");
        assertThat(audit.count()).isEqualTo(0);   // запись аудита тоже откатилась
    }

    @Test
    void requiresNewSuccess() {
        service.withdrawRequiresNew(aliceId, new BigDecimal("100.00"), false);

        assertThat(balance()).isEqualByComparingTo("900.00");
        assertThat(audit.count()).isEqualTo(1);
    }

    @Test
    void requiresNewOuterFails_auditSurvives() {
        assertThatThrownBy(() -> service.withdrawRequiresNew(aliceId, new BigDecimal("100.00"), true))
                .isInstanceOf(IllegalStateException.class);

        assertThat(balance()).isEqualByComparingTo("1000.00");
        assertThat(audit.count()).isEqualTo(1);   // запись аудита осталась
    }
}
