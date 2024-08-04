package dev.kc.scf.order.lambda;

import dev.kc.scf.order.dto.*;
import dev.kc.scf.order.exception.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;

import java.util.function.*;

/**
 * @author Krishna Chaitanya
 */
@Configuration
public class OrderLambda {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderLambda.class);

    @Value("${api.order}")
    private String orderApi;

    private final RestClient restClient;

    public OrderLambda(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Bean("order")
    Consumer<Request<OrderRequestData>> processOrder() {
        return request -> {
            var type = request.type();
            LOGGER.info("Processing order request type : {}", type);
            if ("order".equals(type)) {
                // do some business logic and call Payment API
                LOGGER.info("order api : {}", orderApi);
                var responseEntity = restClient.post().uri(orderApi).body(request.data()).retrieve().toBodilessEntity();
                LOGGER.info("Received order response : {}", responseEntity);
            } else {
                throw new OrderTypeNotFoundException(type);
            }
        };
    }

}
