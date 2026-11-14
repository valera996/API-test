package common.extension;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pagase.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeTestExecutionCallback {
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if(annotation != null){
            int userCount = annotation.value();

            SessionStorage.clear();
            List<CreateUserRequest> users = new LinkedList<>();

            for(int i = 0; i < userCount; i++){
                CreateUserRequest user = AdminSteps.createUser();
                users.add(user);
            }
            SessionStorage.addUsers(users);
            int authAsUser = annotation.auth();
            BasePage.authAsUser(SessionStorage.getUser(authAsUser));
        }
    }
}
