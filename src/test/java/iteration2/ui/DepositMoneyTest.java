package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pagase.BankAlert;
import ui.pagase.DepositMoney;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {

    @Test
    @UserSession
    public void depositMoneyWithValidAmountTest(){
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        double amount = RandomData.getValidDepositAmount();
        new DepositMoney().open().depositMoney(account.getAccountNumber(), amount)
                        .checkAlertMessageAndAccept(String.format(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage(),
                                amount,
                                account.getAccountNumber()));

        double actualBalance = SessionStorage.getSteps().getBalance(account.getAccountNumber());
        assertThat(actualBalance).isEqualTo(amount);
    }

    @Test
    @UserSession
    public void depositMoneyWithinSelectedAccount(){
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        double amount = RandomData.getValidDepositAmount();
        new DepositMoney().open().depositMoney(amount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT.getMessage());

        double actualBalance = SessionStorage.getSteps().getBalance(account.getAccountNumber());
        assertThat(actualBalance).isZero();
    }

    @Test
    @UserSession
    public void depositMoneyWithInvalidAmountTest(){
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        double amount = RandomData.getInvalidDepositAmount();

        new DepositMoney().open().depositMoney(account.getAccountNumber(),amount)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUALS_TO_5000.getMessage());

        double actualBalance = SessionStorage.getSteps().getBalance(account.getAccountNumber());
        assertThat(actualBalance).isZero();
    }
}