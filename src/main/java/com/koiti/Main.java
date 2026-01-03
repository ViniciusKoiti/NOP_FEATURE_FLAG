package com.koiti;

import com.koiti.nop.feature.FeatureFlagRegistry;
import com.koiti.nop.payment.NOPPaymentProcessor;
import com.koiti.traditional.TraditionalFeatureFlagService;
import com.koiti.traditional.TraditionalPaymentProcessor;

/**
 * Demonstração prática das duas abordagens
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Feature Flags: Tradicional vs NOP ===\n");

        demonstrateTraditional();
        System.out.println();
        demonstrateNOP();
        System.out.println();
        demonstratePerformance();
    }

    private static void demonstrateTraditional() {
        System.out.println("--- Abordagem TRADICIONAL ---");
        
        TraditionalFeatureFlagService flagService = new TraditionalFeatureFlagService();
        flagService.setFlag("payment_v2", false);
        
        TraditionalPaymentProcessor processor = new TraditionalPaymentProcessor(flagService);
        
        System.out.println("Processando com flag=false (usa legado):");
        processor.process(100.0);
        
        System.out.println("Mudando flag para true...");
        flagService.setFlag("payment_v2", true);
        
        System.out.println("Processando com flag=true (usa novo):");
        processor.process(100.0);
        
        System.out.println("⚠️  Problema: O 'if' é verificado TODA VEZ");
    }

    private static void demonstrateNOP() {
        System.out.println("--- Abordagem NOP (Notification Oriented) ---");
        
        FeatureFlagRegistry registry = FeatureFlagRegistry.getInstance();
        registry.clear();
        registry.createOrUpdate("payment_v2_nop", false);
        
        NOPPaymentProcessor processor = new NOPPaymentProcessor("payment_v2_nop");
        
        System.out.println("Processando com flag=false (usa legado):");
        processor.process(100.0);
        
        System.out.println("Mudando flag para true...");
        registry.createOrUpdate("payment_v2_nop", true);
        System.out.println("✅ Processor foi NOTIFICADO e trocou a estratégia!");
        
        System.out.println("Processando com flag=true (usa novo):");
        processor.process(100.0);
        
        System.out.println("✅ SEM 'if' em runtime - execução direta!");
    }

    private static void demonstratePerformance() {
        System.out.println("--- Simulação de Performance ---");
        
        // Setup
        TraditionalFeatureFlagService traditionalService = new TraditionalFeatureFlagService();
        traditionalService.setFlag("perf_test", true);
        TraditionalPaymentProcessor traditionalProcessor = 
            new TraditionalPaymentProcessor(traditionalService);
        
        FeatureFlagRegistry registry = FeatureFlagRegistry.getInstance();
        registry.clear();
        registry.createOrUpdate("perf_test_nop", true);
        NOPPaymentProcessor nopProcessor = new NOPPaymentProcessor("perf_test_nop");
        
        int iterations = 10_000_000;
        
        // Tradicional
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            traditionalProcessor.process(100.0);
        }
        long traditionalTime = System.nanoTime() - start;
        
        // NOP
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            nopProcessor.process(100.0);
        }
        long nopTime = System.nanoTime() - start;
        
        // Resultados
        System.out.printf("Iterações: %,d%n", iterations);
        System.out.printf("Tradicional: %.2f ms%n", traditionalTime / 1_000_000.0);
        System.out.printf("NOP: %.2f ms%n", nopTime / 1_000_000.0);
        System.out.printf("Speedup: %.2fx%n", (double) traditionalTime / nopTime);
        System.out.println("\n✅ NOP elimina o overhead do 'if' repetido!");
        System.out.println("💚 Menor uso de CPU = Green Coding!");
    }
}
