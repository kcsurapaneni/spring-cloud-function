package dev.kc.scf.order.exception;

import org.springframework.http.*;
import org.springframework.web.*;

/**
 * @author Krishna Chaitanya
 */
public class OrderTypeNotFoundException extends ErrorResponseException {

    public OrderTypeNotFoundException(String orderType) {
        super(HttpStatus.NOT_FOUND, asProblemDetail("Order with '" + orderType + "' is not found"), null);
    }

    public static ProblemDetail asProblemDetail(String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
        problemDetail.setTitle("Order Type Not Found");
        return problemDetail;
    }

}
