package com.ecommerce.tests.api;

import com.ecommerce.api.utils.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

@Listeners({com.ecommerce.listeners.ApiTestListener.class})
public class BaseApiTest {

    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setUpApi() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URL)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
}
