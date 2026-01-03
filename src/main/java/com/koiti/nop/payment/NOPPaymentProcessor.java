package com.koiti.nop.payment;

import com.koiti.nop.feature.FeatureFlagRegistry;

/**
 * Processador de Pagamento usando Paradigma Orientado a Notificações (NOP)
 * DIFERENÇA CHAVE da abordagem tradicional:
 * - NÃO há "if" no método process()
 * - A estratégia é trocada quando a flag MUDA (reativo)
 * - Execução é DIRETA, sem verificações
 * Resultado: ~9.72x mais rápido (segundo o artigo)
 */
public class NOPPaymentProcessor {
    private volatile PaymentStrategy currentStrategy;
    private final PaymentStrategy newPaymentStrategy;
    private final PaymentStrategy legacyPaymentStrategy;

    public NOPPaymentProcessor(String featureFlagName) {
        this.newPaymentStrategy = new NewPaymentStrategy();
        this.legacyPaymentStrategy = new LegacyPaymentStrategy();
        
        // ✅ Inscreve-se para receber notificações de mudanças
        FeatureFlagRegistry.getInstance().observe(featureFlagName, 
            (name, enabled) -> {
                // Quando a flag MUDA, troca a estratégia
                currentStrategy = enabled ? newPaymentStrategy : legacyPaymentStrategy;
            }
        );
    }

    /**
     * ✅ SEM IF! Execução direta!
     * 
     * O "if" foi resolvido quando a flag mudou,
     * não a cada chamada deste método.
     */
    public void process(double amount) {
        currentStrategy.process(amount);
    }

    // Implementações concretas
    private static class NewPaymentStrategy implements PaymentStrategy {
        @Override
        public void process(double amount) {
            // Simula novo processamento
            double result = amount * 1.01;
        }
    }

    private static class LegacyPaymentStrategy implements PaymentStrategy {
        @Override
        public void process(double amount) {
            // Simula processamento legado
            double result = amount * 1.00;
        }
    }
}
