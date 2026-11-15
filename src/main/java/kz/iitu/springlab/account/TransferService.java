package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository repository;

    public TransferService(AccountRepository repository) {
        this.repository = repository;
    }

    // 1. Обычный перевод из задания
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);
        repository.save(from);
        repository.save(to);
    }

    // 2. Непроверяемое исключение после списания, до зачисления
    @Transactional
    public void transferFailingAfterDebit(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);   // UPDATE реально уходит в базу
        throw new IllegalStateException("Simulated failure after debit");
    }

    // 3. Проверяемое исключение: по умолчанию отката НЕТ
    @Transactional
    public void transferCheckedFailure(Long fromId, Long toId, BigDecimal amount) throws Exception {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);
        throw new Exception("Simulated checked failure");
    }

    // 4. То же, но с rollbackFor: откат ЕСТЬ
    @Transactional(rollbackFor = Exception.class)
    public void transferCheckedFailureWithRollbackFor(Long fromId, Long toId, BigDecimal amount) throws Exception {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);
        throw new Exception("Simulated checked failure");
    }
}
