package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
ADMIN_USER(
        "/admin/users",
        CreateUserRequest.class,
        CreateUserResponse.class
),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetAccountResponse.class
    ),
    CUSTOMER_PROFILE(
            "/customer/profile",
            ChangeUserNameRequest.class,
            GetUserProfileResponse.class
    ),

    ACCOUNTS_DEPOSIT(
            "/accounts/deposit",
            DepositMoneyToAccountRequest.class,
            BaseModel.class
    ),

    ACCOUNTS_TRANSFER(
            "/accounts/transfer",
            TransferMoneyToAnotherAccountRequest.class,
            BaseModel.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

}
