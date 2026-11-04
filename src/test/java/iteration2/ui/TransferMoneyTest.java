package iteration2.ui;

import api.models.*;
import api.requests.steps.UserSteps;
import api.generators.RandomData;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pagase.BankAlert;
import ui.pagase.MakeATransfer;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {

    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;

    @Test
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();

        userSteps.depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                        .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(),
                                transferAmount,
                                recipientAcc.getAccountNumber()));

        double senderBalance = userSteps.getBalance(senderAccount.getAccountNumber());

        double recipientBalance = userSteps.getBalance(recipientAcc.getAccountNumber());
        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT - transferAmount);
        assertThat(recipientBalance).isEqualTo(transferAmount);
    }

    @Test
    public void transferWithoutSenderAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();

        userSteps.depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance = userSteps.getBalance(senderAccount.getAccountNumber());
        double recipientBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferWithoutRecipientAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();

        userSteps.depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransferWithoutRecipientAccount(senderAccount.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance =  userSteps.getBalance(senderAccount.getAccountNumber());
        double recipientBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferAmountAboveMaximumTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();
        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getInvalidTransferAmountAboveMax();
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        double firstAccountBalance =  userSteps.getBalance(senderAccount.getAccountNumber());
        double secondAccountBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    public void transferAmountBelowMinimumTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();
        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getInvalidTransferAmountBelowMin();
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage());

        double firstAccountBalance =  userSteps.getBalance(senderAccount.getAccountNumber());
        double secondAccountBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    public void transferWithoutConfirmationTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();

        userSteps.depositMoney(senderAccount.getId(),MAX_DEPOSIT_AMOUNT);

        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransferWithoutCheckbox(senderAccount.getAccountNumber(), recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        double senderBalance =  userSteps.getBalance(senderAccount.getAccountNumber());
        double recipientBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferWithoutEnoughBalanceTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse senderAccount = userSteps.createAccount();
        CreateAccountResponse recipientAcc = userSteps.createAccount();

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        new MakeATransfer().open().makeATransfer(senderAccount.getAccountNumber(),recipientAcc.getAccountNumber(),user.getUsername(),transferAmount)
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS.getMessage());

        double senderBalance =  userSteps.getBalance(senderAccount.getAccountNumber());
        double recipientBalance = userSteps.getBalance(recipientAcc.getAccountNumber());

        assertThat(senderBalance).isZero();
        assertThat(recipientBalance).isZero();
    }
}
