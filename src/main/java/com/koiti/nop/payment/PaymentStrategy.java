package com.koiti.nop.payment;

/**
 * Strategy para processamento de pagamento
 */
@FunctionalInterface
public interface PaymentStrategy {
    void process(double amount);
}
