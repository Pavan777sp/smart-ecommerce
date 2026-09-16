package com.ecommerce.exception;

/**
 * Thrown when an operation requests more inventory than currently available.
 */
public class InsufficientStockException extends Exception {
    private final int productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = -1;
        this.requestedQuantity = -1;
        this.availableStock = -1;
    }

    public InsufficientStockException(int productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for Product ID %d: requested %d, but only %d available.",
                productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public int getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
