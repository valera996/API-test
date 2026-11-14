package iteration2.ui;

import api.models.*;
import api.generators.RandomData;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pagase.BankAlert;
import ui.pagase.MakeATransfer;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {

    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;

    @Test
    @UserSession
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount =  SessionStorage.getSteps().createAccount();

        SessionStorage.getSteps().depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc =  SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                        .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(),
                                transferAmount,
                                recipientAcc.getAccountNumber()));

        double senderBalance =  SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());

        double recipientBalance =  SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());
        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT - transferAmount);
        assertThat(recipientBalance).isEqualTo(transferAmount);
    }

    @Test
    @UserSession
    public void transferWithoutSenderAccountTest() {
        CreateUserRequest user = SessionStorage.getUser();


        CreateAccountResponse senderAccount =  SessionStorage.getSteps().createAccount();

        SessionStorage.getSteps().depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc =  SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance =  SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double recipientBalance =  SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    @UserSession
    public void transferWithoutRecipientAccountTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount =  SessionStorage.getSteps().createAccount();

        SessionStorage.getSteps().depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc =  SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransferWithoutRecipientAccount(senderAccount.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance =   SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double recipientBalance =  SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    @UserSession
    public void transferAmountAboveMaximumTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();
        CreateAccountResponse recipientAcc = SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getInvalidTransferAmountAboveMax();
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        double firstAccountBalance = SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double secondAccountBalance = SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    @UserSession
    public void transferAmountBelowMinimumTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();
        CreateAccountResponse recipientAcc = SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getInvalidTransferAmountBelowMin();
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage());

        double firstAccountBalance = SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double secondAccountBalance = SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    @UserSession
    public void transferWithoutConfirmationTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();

        SessionStorage.getSteps().depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc = SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransferWithoutCheckbox(senderAccount.getAccountNumber(), recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance =  SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double recipientBalance = SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    @UserSession
    public void transferWithoutEnoughBalanceTest() {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse senderAccount = SessionStorage.getSteps().createAccount();
        CreateAccountResponse recipientAcc = SessionStorage.getSteps().createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS.getMessage());

        double senderBalance =  SessionStorage.getSteps().getBalance(senderAccount.getAccountNumber());
        double recipientBalance = SessionStorage.getSteps().getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isZero();
        assertThat(recipientBalance).isZero();
    }
}
