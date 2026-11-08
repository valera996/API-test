package iteration2.ui;

import api.generators.RandomData;
import api.models.GetUserProfileResponse;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pagase.BankAlert;
import ui.pagase.EditProfile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeNameTest extends BaseUiTest {

    @Test
    @UserSession
    public void changeNameWithValidWord() {
        String newName = RandomData.getValidName();
            new EditProfile().open().changeName(newName)
                    .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

            GetUserProfileResponse actualName = SessionStorage.getSteps().getUserProfile();
            assertThat(newName).isEqualTo(actualName.getName());
    }

    @Test
    @UserSession
    public void changeNameWithInvalidWord() {
        String invalidName = RandomData.getValidName() + "1";

            new EditProfile().open().changeName(invalidName)
                    .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        GetUserProfileResponse actualName = SessionStorage.getSteps().getUserProfile();
        assertThat(actualName.getName()).isNull();
    }
}
