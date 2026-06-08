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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        //Создание нового юзера и аккаунта
        String newUserName = DataClass.randomUserName();
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
                        """, newUserName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String newUserAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, newUserName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        int newUserAccountId = AccountClass.createAccountAndGetAccountId(newUserAuthToken);

        //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": %s
                        }
                """,accountId, newUserAccountId, amount);
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

        //Проверка, что на чужой счёт денеги поступили
        double actualBalance = ProfileInfoClass.returnUserBalance(newUserAuthToken);
        assertEquals(amount,actualBalance,0.01);
        //Проверка, что с текущего счета деньги списались
        assertEquals(10000.00 - amount, ProfileInfoClass.returnUserBalance(userAuthToken),0.01);
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

        //Проверка, что на другой счёт денеги поступили
        double actualBalance = AccountClass.getAccountBalanceById(userAuthToken, anotherAccountId);
        assertEquals(amount,actualBalance, 0.01);
        //Проверка, что с текущего счета деньги списались
        assertEquals(10000.00 - amount, AccountClass.getAccountBalanceById(userAuthToken, accountId),0.01);
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

        //Создание нового юзера и аккаунта
        String newUserName = DataClass.randomUserName();
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
                        """, newUserName))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String newUserAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, newUserName))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        int newUserAccountId = AccountClass.createAccountAndGetAccountId(newUserAuthToken);

        //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": %s
                        }
                """,accountId, newUserAccountId, amount);
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

        //Проверка, что на чужой счёт денеги не поступили
        double actualBalance = ProfileInfoClass.returnUserBalance(newUserAuthToken);
        assertEquals(0.00,actualBalance,0.01);
        //Проверка, что с текущего счета деньги не списались
        assertEquals(0.00, ProfileInfoClass.returnUserBalance(userAuthToken),0.01);
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

        //Проверка, что на другой счёт денеги не поступили
        double actualBalance = AccountClass.getAccountBalanceById(userAuthToken, anotherAccountId);
        assertEquals(0.00,actualBalance, 0.01);
        //Проверка, что с текущего счета деньги списались
        assertEquals(0.00, AccountClass.getAccountBalanceById(userAuthToken, accountId),0.01);
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

       //Создание нового юзера и аккаунта
       String newUserName = DataClass.randomUserName();
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
                        """, newUserName))
               .post("http://localhost:4111/api/v1/admin/users")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
       String newUserAuthToken = given()
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, newUserName))
               .post("http://localhost:4111/api/v1/auth/login")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .extract()
               .header("Authorization");
       int newUserAccountId = AccountClass.createAccountAndGetAccountId(newUserAuthToken);

       //Перевод денег на чужой аккаунт
        String requestBodyToTransfer = String.format("""
                  {
                        "senderAccountId": %s,
                        "receiverAccountId": %s,
                        "amount": 1000
                        }
                """,accountId, newUserAccountId);
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

       //Проверка, что на чужой счёт денеги не поступили
       double actualBalance = AccountClass.getAccountBalanceById(userAuthToken, newUserAccountId);
       assertEquals(0.00,actualBalance, 0.01);
       //Проверка, что с текущего счета деньги списались
       assertEquals(0.00, AccountClass.getAccountBalanceById(userAuthToken, accountId),0.01);

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

        //Проверка, что на другой счёт денеги не поступили
        double actualBalance = AccountClass.getAccountBalanceById(userAuthToken, anotherAccountId);
        assertEquals(0.00,actualBalance, 0.01);
        //Проверка, что с текущего счета деньги списались
        assertEquals(0.00, AccountClass.getAccountBalanceById(userAuthToken, accountId),0.01);
    }
}
