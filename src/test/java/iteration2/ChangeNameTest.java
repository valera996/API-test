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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.restassured.RestAssured.given;

public class ChangeNameTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new ResponseLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void changeNameWithValidWord(){
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

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                        {
                          "name": "Kate Smith"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Profile updated successfully"));

        String actualName = ProfileInfoClass.returnUserName(userAuthToken);
        assertEquals("Kate Smith", actualName);
    }


    @ParameterizedTest
    @ValueSource(strings = {"Kate1 Smith", "Kate", "Kate Ivanovna Smith"})
    public void changeNameWithInvalidWord(String newName){
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

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(String.format("""
                        {
                          "name": "%s"
                        }
                        """,newName))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Name must contain two words with letters only"));

        String actualName = ProfileInfoClass.returnUserName(userAuthToken);
        assertEquals(null, actualName);
    }

}
