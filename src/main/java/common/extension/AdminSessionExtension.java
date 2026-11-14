package common.extension;

import api.models.CreateUserRequest;
import common.annotation.AdminSession;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pagase.BasePage;

public class AdminSessionExtension implements BeforeTestExecutionCallback {
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        AdminSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if(annotation != null){
            BasePage.authAsUser(CreateUserRequest.getAdmin());
        }

    }
}
