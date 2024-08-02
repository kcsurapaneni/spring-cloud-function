package dev.kc.scf.order.lambda;

import dev.kc.scf.order.dto.*;
import dev.kc.scf.order.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.client.*;
import org.springframework.cloud.function.context.*;
import org.springframework.cloud.function.context.test.*;
import org.springframework.test.web.client.*;

import java.util.*;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@FunctionalSpringBootTest
@AutoConfigureMockRestServiceServer
class OrderLambdaIntegrationTest {

    @Autowired
    private FunctionCatalog functionCatalog;

    @Autowired
    private MockRestServiceServer mockServer;

    @Value("${api.order}")
    private String orderApi;

    @Test
    void testOrderType() {
        // Given
        var items = List.of(new OrderRequestData.Item("abc123", 2), new OrderRequestData.Item("def456", 1));
        var data = new OrderRequestData("12345", 250.00, items);
        var request = new Request<>("order", data);

        // When
        mockServer
                .expect(requestTo(orderApi))
                .andExpect(method(POST))
                .andRespond(withStatus(ACCEPTED));

        Consumer<Request<OrderRequestData>> orderFunction = functionCatalog.lookup(Consumer.class, "order");

        // Then
        assertDoesNotThrow(() -> orderFunction.accept(request));
    }


    @Test
    void invalidOrderType() {
        // Given
        var items = List.of(new OrderRequestData.Item("abc123", 2), new OrderRequestData.Item("def456", 1));
        var data = new OrderRequestData("12345", 250.00, items);
        var request = new Request<>("invalid", data);

        // When
        mockServer
                .expect(requestTo(orderApi))
                .andExpect(method(POST))
                .andRespond(withStatus(ACCEPTED));

        Consumer<Request<OrderRequestData>> orderFunction = functionCatalog.lookup(Consumer.class, "order");

        // Then
        assertThrowsExactly(OrderTypeNotFoundException.class, () -> orderFunction.accept(request));
    }

}