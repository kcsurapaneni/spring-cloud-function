package dev.kc.scf.order.lambda;

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;
import com.amazonaws.services.lambda.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.*;
import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.springframework.boot.test.context.*;
import org.testcontainers.containers.*;
import org.testcontainers.containers.localstack.*;
import org.testcontainers.containers.output.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.*;
import org.wiremock.integrations.testcontainers.*;

import java.nio.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.*;
import static org.testcontainers.utility.DockerImageName.*;

/**
 * @author Krishna Chaitanya
 */
@Testcontainers
@SpringBootTest
class TestAWSIntegrationOrderLambda {
    // To attach logs of the container
    private final static Logger LOGGER = LoggerFactory.getLogger(TestAWSIntegrationOrderLambda.class);

    public static final String FUNCTION_NAME = "order-lambda";

    private static final Network network = Network.newNetwork();

    @Container
    static LocalStackContainer localStackContainer = new LocalStackContainer(parse("localstack/localstack:3.6.0"))
            .withNetwork(network)
            .withNetworkAliases("localstack")
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .withServices(LocalStackContainer.Service.LAMBDA)
            .withEnv("DEBUG", "1");

    @Container
    static WireMockContainer wireMockContainer = new WireMockContainer(parse("wiremock/wiremock:3.9.1"))
            .withNetwork(network)
            .withNetworkAliases("wiremock")
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .withMappingFromResource("mock-response.json");

    @Test
    void testLambdaFunction() throws Exception {
        var endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
                localStackContainer.getEndpoint().toString(),
                localStackContainer.getRegion());
        var awsLambda = AWSLambdaClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .build();

        // Set up environment variables
        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("api.order", "http://wiremock:8080/new");

        CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
                .withFunctionName(FUNCTION_NAME)
                .withRuntime(Runtime.Java21)
                .withEnvironment(new Environment().withVariables(environmentVariables))
                .withHandler("org.springframework.cloud.function.adapter.aws.FunctionInvoker")
                .withCode(new FunctionCode()
                        .withZipFile(ByteBuffer.wrap(Files.readAllBytes(Paths.get("target", "scf-0.0.1-SNAPSHOT-aws.jar")))))
                .withRole("arn:aws:iam::000000000000:role/lambda-role");
        awsLambda.createFunction(createFunctionRequest);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(3))
                .until(() -> {
                    try {
                        awsLambda.getFunction(new GetFunctionRequest().withFunctionName(FUNCTION_NAME));
                        LOGGER.info("Function loaded");
                        return true;
                    } catch (ResourceNotFoundException e) {
                        LOGGER.info("Function not found, try again");
                        return false;
                    }
                });

        var invokeRequest = new InvokeRequest()
                .withFunctionName(FUNCTION_NAME)
                .withPayload("""
                        {
                            "type": "order",
                            "data": {
                                "orderId": "12345",
                                "amount": 250.00,
                                "items": [
                                    {
                                        "itemId": "abc123",
                                        "quantity": 2
                                    },
                                    {
                                        "itemId": "def456",
                                        "quantity": 1
                                    }
                                ]
                            }
                        }
                        """);
        var invokeResult = awsLambda.invoke(invokeRequest);
        Assertions.assertNull(invokeResult.getFunctionError());
    }
}
