package com.koiti.traditional;

/**
 * Processador de Pagamento TRADICIONAL
 * 
 * Verifica o feature flag a CADA execução
 */
public class TraditionalPaymentProcessor {
    private final TraditionalFeatureFlagService featureFlagService;
    private final NewPaymentProcessor newProcessor;
    private final LegacyPaymentProcessor legacyProcessor;

    public TraditionalPaymentProcessor(TraditionalFeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
        this.newProcessor = new NewPaymentProcessor();
        this.legacyProcessor = new LegacyPaymentProcessor();
    }

    public void process(double amount) {
        // ❌ IF executado TODA VEZ - mesmo quando o flag não mudou!
        if (featureFlagService.isEnabled("new_payment")) {
            newProcessor.process(amount);
        } else {
            legacyProcessor.process(amount);
        }
    }

    // Simulação dos processadores
    private static class NewPaymentProcessor {
        public void process(double amount) {
            // Simula processamento novo
            double result = amount * 1.01;
        }
    }

    private static class LegacyPaymentProcessor {
        public void process(double amount) {
            // Simula processamento legado
            double result = amount * 1.00;
        }
    }
}
