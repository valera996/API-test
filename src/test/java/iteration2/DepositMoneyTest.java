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


public class DepositMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new ResponseLoggingFilter(),
                        new ResponseLoggingFilter()));
    }
    @ParameterizedTest
    @ValueSource(doubles ={0.01, 4999.99, 5000.00})
    public void depositMoneyWithValidAmountTest(double amount ){
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

        //Депосит денег на созданный аккаунт
        DepositClass.depositMoneyToAccount(amount, accountId, userAuthToken);
    }
    public static Stream<Arguments> invalidAmount(){
        return Stream.of(
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000")
        );
    }
    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void depositMoneyWithInvalidAmountTest(double amount, String errorValue ){
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

        //Депосит денег на созданный аккаунт
        String requestBody = String.format("""
                  {
                          "id": %s,
                          "balance": %s
                        }
                """, accountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }

    @Test
    public void depositMoneyToAnotherPersonAccountTest(){
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

        //Депосит денег на чужой аккаунт
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                  {
                          "id": 1,
                          "balance": 300
                        }
                """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }
    @Test
    public void depositMoneyToNonexistentPersonAccountTest(){
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

        //Депосит денег на чужой аккаунт
        int invalidId = Integer.MAX_VALUE;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(String.format("""
                  {
                          "id": %s,
                          "balance": 300
                        }
                """,invalidId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }
}
