package ui.pagase;

import api.Specs.RequestSpecs;
import api.models.CreateUserRequest;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class BasePage<T extends BasePage>{

    protected SelenideElement userNameInput = $(Selectors.byAttribute("placeHolder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeHolder", "Password"));
    public abstract String url();

    public T open(){
            return Selenide.open(url(), (Class<T>) this.getClass());
    }
    public <T extends BasePage> T getPage(Class<T> pageClass) {return Selenide.page(pageClass); }

    public  T checkAlertMessageAndAccept(String bankAlert){
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }
    public static void authAsUser(String username, String password){
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", RequestSpecs.getUserAuthHeader(username,password));
    }

    public static void authAsUser(CreateUserRequest createUserRequest){
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor){
        return elementsCollection.stream().map(constructor).toList();
    }

}
