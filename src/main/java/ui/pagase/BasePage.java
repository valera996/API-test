package ui.pagase;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
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
}
