package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.DepositMoneyToAccountRequest;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class DepositMoneyToAccountRequester extends Request<DepositMoneyToAccountRequest>{
    public DepositMoneyToAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(DepositMoneyToAccountRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(DepositMoneyToAccountRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse put(DepositMoneyToAccountRequest model) {
        return null;
    }
}
