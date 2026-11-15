package kz.iitu.springlab.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    TransferService service;
    @Autowired
    AccountRepository repository;

    private Long aliceId;
    private Long bobId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        aliceId = repository.save(new Account("Alice", new BigDecimal("1000.00"))).getId();
        bobId = repository.save(new Account("Bob", new BigDecimal("500.00"))).getId();
    }

    private BigDecimal balance(Long id) {
        return repository.findById(id).orElseThrow().getBalance();
    }

    @Test
    void successfulTransfer() {
        service.transfer(aliceId, bobId, new BigDecimal("100.00"));

        assertThat(balance(aliceId)).isEqualByComparingTo("900.00");
        assertThat(balance(bobId)).isEqualByComparingTo("600.00");
    }

    @Test
    void insufficientFundsChangesNothing() {
        assertThatThrownBy(() -> service.transfer(aliceId, bobId, new BigDecimal("5000.00")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }

    @Test
    void uncheckedExceptionRollsBack() {
        assertThatThrownBy(() -> service.transferFailingAfterDebit(aliceId, bobId, new BigDecimal("100.00")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }

    @Test
    void checkedExceptionDoesNotRollBackByDefault() {
        assertThatThrownBy(() -> service.transferCheckedFailure(aliceId, bobId, new BigDecimal("100.00")))
                .hasMessage("Simulated checked failure");

        // списание сохранилось: транзакция была зафиксирована
        assertThat(balance(aliceId)).isEqualByComparingTo("900.00");
    }

    @Test
    void checkedExceptionRollsBackWithRollbackFor() {
        assertThatThrownBy(() -> service.transferCheckedFailureWithRollbackFor(aliceId, bobId, new BigDecimal("100.00")))
                .hasMessage("Simulated checked failure");

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
    }
}
