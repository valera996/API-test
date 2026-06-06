package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new ResponseLoggingFilter(),
                        new ResponseLoggingFilter()));
    }
    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheAnotherOwnerTest(double amount){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Депосит 10000 денег на созданный аккаунт, чтобы хватило на перевод (по 5к т.к. ограничение)
        DepositClass.depositMoneyToAccount(5000, accountId, userAuthToken);
        DepositClass.depositMoneyToAccount(5000, accountId, userAuthToken);

        //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": 1,
                        "amount": %s
                        }
                """,accountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest(double amount){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //Получение id аккаунта(счёта) юзера
        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);
        int anotherAccountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Депосит 10000 денег на созданный аккаунт, чтобы хватило на перевод (по 5к т.к. ограничение)
        DepositClass.depositMoneyToAccount(5000, accountId, userAuthToken);
        DepositClass.depositMoneyToAccount(5000, accountId, userAuthToken);

        //Перевод денег между аккаунтами юзера
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": %s
                        }
                """,accountId, anotherAccountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"));
    }

    public static Stream<Arguments> invalidAmount(){
        return Stream.of(
                Arguments.of(-0.01,"Transfer amount must be at least 0.01"),
                Arguments.of(10000.01,"Transfer amount cannot exceed 10000")
        );
    }
    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void transferInvalidAmountFromOneAccountToAnotherAccountWithTheAnotherOwnerTest(double amount, String errorValue){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": 1,
                        "amount": %s
                        }
                """,accountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }

    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void transferInvalidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest(double amount, String errorValue){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //Получение id аккаунта(счёта) юзера
        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);
        int anotherAccountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Перевод денег между аккаунтами юзера
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": %s
                        }
                """,accountId, anotherAccountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }

   @Test
    public void transferFromOneAccountWithoutEnoughBalanceToAnotherAccountWithTheAnotherOwnerTest(){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": 1,
                        "amount": 1000
                        }
                """,accountId);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @Test
    public void transferFromOneAccountWithoutEnoughBalanceToAnotherAccountWithTheTheSameOwnerTest(){
        String userName = DataClass.randomUserName();
        //Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                         "username": "%s",
                         "password": "Kate2000#",
                         "role": "USER"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //Получение токена юзера
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, userName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //Получение id аккаунта(счёта) юзера
        int accountId = AccountClass.createAccountAndGetAccountId(userAuthToken);
        int anotherAccountId = AccountClass.createAccountAndGetAccountId(userAuthToken);

        //Перевод денег между аккаунтами юзера
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": 1000
                        }
                """,accountId, anotherAccountId);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBodyToTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }
}
