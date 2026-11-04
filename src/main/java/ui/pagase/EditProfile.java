package ui.pagase;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$;

public class EditProfile extends BasePage<EditProfile>{
    private SelenideElement newNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangeButton = $(Selectors.byTagAndText("button","💾 Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile changeName(String newName) {
        newNameInput.click();
        newNameInput.setValue(newName);
        newNameInput.clear();
        newNameInput.setValue(newName);
        saveChangeButton.click();
        return this;
    }
}
