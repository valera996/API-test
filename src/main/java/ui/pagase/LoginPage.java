package ui.pagase;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage>{
    private SelenideElement button = $("button");

    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String userName, String password){
        userNameInput.sendKeys(userName);
        passwordInput.sendKeys(password);
        button.click();
        return this;
    }

}
