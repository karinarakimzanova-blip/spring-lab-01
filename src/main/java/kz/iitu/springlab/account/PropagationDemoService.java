package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PropagationDemoService {

    private final AccountRepository accounts;
    private final AuditService audit;

    public PropagationDemoService(AccountRepository accounts, AuditService audit) {
        this.accounts = accounts;
        this.audit = audit;
    }

    @Transactional
    public void withdrawRequired(Long accountId, BigDecimal amount, boolean fail) {
        Account account = accounts.findById(accountId).orElseThrow();
        account.debit(amount);
        accounts.save(account);
        audit.logRequired("Withdrawn " + amount + " (REQUIRED)");
        if (fail) throw new IllegalStateException("Outer failure");
    }

    @Transactional
    public void withdrawRequiresNew(Long accountId, BigDecimal amount, boolean fail) {
        Account account = accounts.findById(accountId).orElseThrow();
        account.debit(amount);
        accounts.save(account);
        audit.logRequiresNew("Withdrawn " + amount + " (REQUIRES_NEW)");
        if (fail) throw new IllegalStateException("Outer failure");
    }
}
