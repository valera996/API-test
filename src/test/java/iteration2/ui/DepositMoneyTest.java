package iteration2.ui;

import api.requests.steps.UserSteps;
import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pagase.BankAlert;
import ui.pagase.DepositMoney;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {

    @Test
    public void depositMoneyWithValidAmountTest(){
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse account = userSteps.createAccount();

        double amount = RandomData.getValidDepositAmount();
        new DepositMoney().open().depositMoney(account.getAccountNumber(), amount)
                        .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage(),
                                amount,
                                account.getAccountNumber()));

        double actualBalance = userSteps.getBalance(account.getAccountNumber());
        assertThat(actualBalance).isEqualTo(amount);
    }

    @Test
    public void depositMoneyWithinSelectedAccount(){
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse account = userSteps.createAccount();

        double amount = RandomData.getValidDepositAmount();
        new DepositMoney().open().depositMoney(amount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT.getMessage());

        double actualBalance = userSteps.getBalance(account.getAccountNumber());
        assertThat(actualBalance).isZero();
    }

    @Test
    public void depositMoneyWithInvalidAmountTest(){
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());
        CreateAccountResponse account = userSteps.createAccount();

        double amount = RandomData.getInvalidDepositAmount();

        new DepositMoney().open().depositMoney(account.getAccountNumber(),amount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUALS_TO_5000.getMessage());

        double actualBalance = userSteps.getBalance(account.getAccountNumber());
        assertThat(actualBalance).isZero();
    }
}