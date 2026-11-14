package iteration2.ui;

import api.requests.steps.UserSteps;
import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.GetUserProfileResponse;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pagase.BankAlert;
import ui.pagase.EditProfile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeNameTest extends BaseUiTest {

    @Test
    public void changeNameWithValidWord() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String newName = RandomData.getValidName();
            new EditProfile().open().changeName(newName)
                    .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

            GetUserProfileResponse actualName = new UserSteps(user.getUsername(), user.getPassword()).getUserProfile();
            assertThat(newName).isEqualTo(actualName.getName());
    }

    @Test
    public void changeNameWithInvalidWord() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        String invalidName = RandomData.getValidName() + "1";

            new EditProfile().open().changeName(invalidName)
                    .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        GetUserProfileResponse actualName = new UserSteps(user.getUsername(), user.getPassword()).getUserProfile();
        assertThat(actualName.getName()).isNull();
    }
}
