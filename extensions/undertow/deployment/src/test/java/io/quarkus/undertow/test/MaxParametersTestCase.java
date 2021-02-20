package io.quarkus.undertow.test;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class MaxParametersTestCase {

    @RegisterExtension
    static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestServlet.class, TestGreeter.class)
                    .addAsResource(new StringAsset("quarkus.servlet.max-parameters=10"), "application.properties"));

    @Test
    public void testSmallRequest() {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            params.put(String.format("q%d", i), String.format("%d", i));
        }

        RestAssured.given()
                .params(params)
                .get("/test")
                .then().statusCode(200).body(Matchers.equalTo("test servlet"));
    }

    @Test
    public void testLargeRequest() {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < 11; i++) {
            params.put(String.format("q%d", i), String.format("%d", i));
        }

        // At the moment, a throw of io.undertow.util.ParameterLimitException causes a failure
        // to respond to the HTTP request.
        Assertions.assertThrows(SocketTimeoutException.class, () -> RestAssured.given()
                .params(params)
                .get("/test")
                .then().statusCode(414));
    }
}
