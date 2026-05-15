package com.example.xbankbackend.services.savings;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.TransactionType;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.SavingsAccount;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.SavingsAccountRepository;
import com.example.xbankbackend.services.AppSettingsService;
import com.example.xbankbackend.services.bankAccount.BankAccountService;
import com.example.xbankbackend.services.transaction.TransactionsService;
import com.example.xbankbackend.services.user.UserValidationService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class SavingsAccountService {

    private SavingsAccountRepository savingsAccountRepository;
    private BankAccountRepository bankAccountRepository;
    private TransactionsService transactionsService;
    private AppSettingsService appSettingsService;

    private final SavingsAccountValidationService savingsAccountValidationService;
    private final UserValidationService userValidationService;

    private static final int DAYS_IN_YEAR = 365;

    public void create(SavingsAccount savingsAccount) {
        UUID baseAccountId = savingsAccount.getAccountId();

        savingsAccountValidationService.validateSavingsAccountUnique(baseAccountId);

        BankAccount baseAccount = bankAccountRepository.get(baseAccountId);

        if (baseAccount.getAccountType() != BankAccountType.SAVINGS) {
            throw new IllegalArgumentException("Счёт должен быть типа SAVINGS");
        }

        BigDecimal interest = appSettingsService.getSavingsRate(savingsAccount.isAllowWithdrawal(), savingsAccount.isAllowTopUp());

        savingsAccount.setInterestRate(interest);

        // TODO: подтягивать penalty из настроек

        savingsAccountRepository.create(savingsAccount);
    }

    public BigDecimal getInterest(boolean allowWithdrawal, boolean allowTopUp) {
        return appSettingsService.getSavingsRate(allowWithdrawal, allowTopUp);
    }

    public SavingsAccount get(UUID accountId) {
        savingsAccountValidationService.validateSavingsAccountExists(accountId);

        return savingsAccountRepository.get(accountId);
    }

    public List<SavingsAccount> getByUserId(UUID userId) {
        userValidationService.validateUserExists(userId);

        List<SavingsAccount> savingsAccounts = new ArrayList<>();

        for (BankAccount account : bankAccountRepository.getBankAccounts(userId)) {
            UUID accountId = account.getAccountId();

            if (savingsAccountRepository.exists(accountId)) {
                savingsAccounts.add(savingsAccountRepository.get(accountId));
            }
        }

        return savingsAccounts;
    }

    public List<SavingsAccount> getAllActive() {
        return savingsAccountRepository.findAllActive();
    }

    public SavingsAccount prolong(UUID accountId, LocalDate newMaturityDate) {
        savingsAccountValidationService.validateSavingsAccountExists(accountId);

        SavingsAccount savingsAccount = savingsAccountRepository.get(accountId);

        if (!savingsAccount.getStatus().equals("EXPIRED")) {
            throw new AccessDeniedException("Можно продлить действие вклада только с истёкшим сроком действия");
        }

        savingsAccount.setMaturityDate(newMaturityDate);
        savingsAccount.setLastInterestCalculation(LocalDate.now());

        savingsAccountRepository.update(savingsAccount);

        log.info("Account {} prolonged until {}", accountId, newMaturityDate);

        return savingsAccount;
    }

    public void closeAccount(UUID accountId, UUID targetAccountId, UUID userId) {
        savingsAccountValidationService.validateSavingsAccountExists(accountId);
        if (accountId == targetAccountId) {
            throw new IllegalArgumentException("Нельзя перевести деньги на этот же счёт");
        }

        BankAccount sourceBankaccount = bankAccountRepository.get(accountId);
        SavingsAccount savingsAccount = savingsAccountRepository.get(accountId);

        if (savingsAccount.getStatus().equals("CLOSED")) {
            throw new AccessDeniedException("Счёт уже закрыт");
        }

        BigDecimal amount = sourceBankaccount.getBalance().add(savingsAccount.getAccruedInterest());
        if (savingsAccount.getStatus().equals("ACTIVE")) {
            BigDecimal penaltyAmount = savingsAccount.getAccruedInterest()
                    .multiply(savingsAccount.getEarlyWithdrawalPenalty())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            amount = amount.subtract(penaltyAmount);

            log.info("Early closure penalty applied: {}", penaltyAmount);
        }

        makeClosingAccountTransfer(accountId, targetAccountId, amount, userId);

        savingsAccount.setStatus("CLOSED");
        savingsAccountRepository.update(savingsAccount);
        bankAccountRepository.deactivate(accountId);

        log.info("Savings account {} closed. Transferred {} to {}", accountId, amount, targetAccountId);
    }

    public void calculateDailyInterest() {
        LocalDate today = LocalDate.now();
        List<SavingsAccount> accounts = savingsAccountRepository.findForInterestCalculation(today);

        for (SavingsAccount savingsAccount : accounts) {
            UUID savingsAccountId = savingsAccount.getAccountId();
            BankAccount baseAccount = bankAccountRepository.get(savingsAccountId);

            BigDecimal dailyInterest = calculateDailyInterest(baseAccount.getBalance(), savingsAccount.getInterestRate());

            savingsAccountRepository.increaseAccruedInterest(savingsAccountId, dailyInterest);
            savingsAccountRepository.updateLastInterestCalculation(savingsAccountId, today);

            log.info("Accrued daily interest {} for account {}", dailyInterest, savingsAccountId);

            if (savingsAccount.getMaturityDate().equals(today)) {
                savingsAccountRepository.setExpired(savingsAccountId);
            }
        }
    }

    private BigDecimal calculateDailyInterest(BigDecimal balance, BigDecimal annualRate) {
        if (balance == null || annualRate == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, RoundingMode.HALF_UP);
        return balance.multiply(dailyRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void makeMonthlyAccrual() {
        LocalDate today = LocalDate.now();
        List<SavingsAccount> accounts = savingsAccountRepository.findForInterestCalculation(today);

        for (SavingsAccount savings : accounts) {
            BankAccount baseAccount = bankAccountRepository.get(savings.getAccountId());
            BigDecimal accruedInterest = savings.getAccruedInterest();
            UUID accountId = baseAccount.getAccountId();

            makeMonthlyInterestDeposit(accountId, accruedInterest);
            savingsAccountRepository.setAccruedInterestToZero(accountId);

            log.info("Made monthly accrual {} for account {}", accruedInterest, savings.getAccountId());
        }
    }

    private void makeClosingAccountTransfer(UUID from, UUID to, BigDecimal amount, UUID userId) {
        Transaction tx = new Transaction();
        tx.setTransactionType(TransactionType.TRANSFER);
        tx.setSenderId(from);
        tx.setReceiverId(to);
        tx.setAmount(amount);
        tx.setCurrency(bankAccountRepository.getCurrency(from));
        tx.setComment("Перевод с накопительного счёта");

        transactionsService.transfer(tx, userId, true);
    }

    private void makeMonthlyInterestDeposit(UUID to, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setTransactionType(TransactionType.DEPOSIT);
        tx.setReceiverId(to);
        tx.setAmount(amount);
        tx.setCurrency(bankAccountRepository.getCurrency(to));
        tx.setComment("Ежемесячные проценты с накопительного счёта");

        transactionsService.deposit(tx, true);
    }
}