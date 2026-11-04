package iteration1.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pagase.BankAlert;
import ui.pagase.UserDashboard;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest{

    @Test
    public void userCanCreateAccountTest(){

        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccount();

        assertThat(createdAccounts).hasSize(1);
        new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
