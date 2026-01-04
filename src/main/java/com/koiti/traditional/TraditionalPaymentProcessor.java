package com.koiti.traditional;

/**
 * Processador de Pagamento TRADICIONAL
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

    public double process(double amount) {
        if (featureFlagService.isEnabled("new_payment")) {
            return newProcessor.process(amount);
        } else {
            return legacyProcessor.process(amount);
        }
    }

    private static class NewPaymentProcessor {
        public double process(double amount) {
            // Simula processamento novo
            double result = amount * 1.01;
            return result;
        }
    }

    private static class LegacyPaymentProcessor {
        public double process(double amount) {
            // Simula processamento legado
            double result = amount * 1.00;
            return result;
        }
    }
}
