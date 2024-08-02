package dev.kc.scf.order.dto;

import java.util.*;

/**
 * @author Krishna Chaitanya
 */
public record OrderRequestData(String orderId, double amount, List<Item> items) {
    public record Item(String itemId, int quantity) {}

    /**
     * {
     *   "type": "order",
     *   "data": {
     *     "orderId": "12345",
     *     "amount": 250.00,
     *     "items": [
     *       { "itemId": "abc123", "quantity": 2 },
     *       { "itemId": "def456", "quantity": 1 }
     *     ]
     *   }
     * }
     */
}
