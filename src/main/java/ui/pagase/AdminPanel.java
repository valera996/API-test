package ui.pagase;

import com.codeborne.selenide.*;
import lombok.Getter;
import ui.elements.UserBage;
import java.util.List;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel>{
    private SelenideElement addUserbutton = $(Selectors.byText("Add User"));
    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));
    private SelenideElement allUsers =  $(Selectors.byText("All Users"));

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

    public List<UserBage> getAllUsers(){
        ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
        return generatePageElements(elementsCollection, UserBage::new);
    }
    public AdminPanel waitForUser(String username) {
                allUsers.parent()
                .findAll("li")
                .findBy(Condition.text(username))
                .shouldBe(Condition.visible);

        return this;
    }

}
