package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;

public class AccountClass {

    //Создание и получение id аккаунта(счёта) юзера
    public static int createAccountAndGetAccountId(String userAuthToken){
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath()
                .getInt("id");
    }

    //Возвращает баланс по id аккаунта
    public static double getAccountBalanceById(String userAuthToken, int accountId){
         var response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

         List<Integer> accountsIds = response.jsonPath().getList("id", Integer.class);
         List<Double> accountsBalances = response.jsonPath().getList("balance", Double.class);

         for(int i =0; i < accountsIds.size(); i++){
            if(accountsIds.get(i) == accountId){
                return accountsBalances.get(i);
            }
         }
         return 0;
    }
}
