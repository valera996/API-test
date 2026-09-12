package Specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginUserRequest;
import requests.LoginUserRequester;

import java.util.List;

public class RequestSpecs {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private RequestSpecs(){}

    private static RequestSpecBuilder defaultRequestBuilder(){
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new ResponseLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unauthSpec(){
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec(){
        return defaultRequestBuilder()
                .addHeader(AUTHORIZATION_HEADER, "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authAsUser(String userName, String password){
        String userAuthToken = new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(userName).password(password).build())
                .extract()
                .header(AUTHORIZATION_HEADER);

        return defaultRequestBuilder()
                .addHeader(AUTHORIZATION_HEADER,userAuthToken)
                .build();
    }
}
