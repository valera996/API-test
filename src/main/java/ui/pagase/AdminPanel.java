package ui.pagase;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel>{
    private SelenideElement addUserbutton = $(Selectors.byText("Add User"));
    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password){
        userNameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserbutton.click();

        return this;
    }

    public ElementsCollection getAllUsers(){
        return $(Selectors.byText("All Users")).parent().findAll("li");
    }
}
