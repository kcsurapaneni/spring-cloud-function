package dev.kc.scf.order.dto;

/**
 * @author Krishna Chaitanya
 */
public record Request<T>(String type, T data) {
}
