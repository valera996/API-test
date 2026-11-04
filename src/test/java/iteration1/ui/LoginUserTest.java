package iteration1.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pagase.AdminPanel;
import ui.pagase.LoginPage;
import ui.pagase.UserDashboard;

public class LoginUserTest extends BaseUiTest{

    @Test
    public void adminCanLoginWithCorrectDataTest(){
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                        .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest(){
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText().shouldBe(Condition.visible)
                                             .shouldHave(Condition.text("Welcome, noname!"));
    }
}
