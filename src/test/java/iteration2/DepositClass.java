package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class DepositClass {
    //Депосит денег на созданный аккаунт
   public static void depositMoneyToAccount(double amount, int accountId, String userAuthToken) {
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
               .statusCode(HttpStatus.SC_OK);
   }
}
