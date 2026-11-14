package iteration1.ui;

import api.Config.Config;
import api.Specs.RequestSpecs;
import api.models.CreateUserRequest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extension.AdminSessionExtension;
import common.extension.BrowserMathExtension;
import common.extension.UserAgentExtension;
import common.extension.UserSessionExtension;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Map;
import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMathExtension.class)
@ExtendWith(UserAgentExtension.class)
public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid(){

        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true,
                "enableLog", true));
    }
    public void authAsUser(String username, String password){
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", RequestSpecs.getUserAuthHeader(username,password));
    }

    public void authAsUser(CreateUserRequest createUserRequest){
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }
}
