package com.koiti.benchmark;

import com.koiti.nop.feature.FeatureFlagRegistry;
import com.koiti.nop.payment.NOPPaymentProcessor;
import com.koiti.traditional.TraditionalFeatureFlagService;
import com.koiti.traditional.TraditionalPaymentProcessor;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparando abordagem Tradicional vs NOP
 * 
 * Hipótese (baseada no artigo):
 * - NOP deve ser ~9.72x mais rápido
 * - Menos overhead de CPU
 * - Menos branch prediction failures
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class FeatureFlagBenchmark {

    // Estado do Benchmark
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        // Abordagem Tradicional
        TraditionalFeatureFlagService traditionalService;
        TraditionalPaymentProcessor traditionalProcessor;

        // Abordagem NOP
        NOPPaymentProcessor nopProcessor;

        @Setup(Level.Trial)
        public void setup() {
            // Setup Tradicional
            traditionalService = new TraditionalFeatureFlagService();
            traditionalService.setFlag("new_payment", true);
            traditionalProcessor = new TraditionalPaymentProcessor(traditionalService);

            // Setup NOP
            FeatureFlagRegistry registry = FeatureFlagRegistry.getInstance();
            registry.clear();
            registry.createOrUpdate("new_payment_nop", true);
            nopProcessor = new NOPPaymentProcessor("new_payment_nop");
        }
    }

    /**
     * Benchmark da abordagem TRADICIONAL
     * - Verifica o flag a cada chamada
     * - Overhead de if + branch prediction
     */
    @Benchmark
    public void traditionalApproach(BenchmarkState state) {
        state.traditionalProcessor.process(100.0);
    }

    /**
     * Benchmark da abordagem NOP
     * - SEM if em runtime
     * - Execução direta
     */
    @Benchmark
    public void nopApproach(BenchmarkState state) {
        state.nopProcessor.process(100.0);
    }

    /**
     * Benchmark com mudança frequente de flag (Traditional)
     * Simula cenário onde o flag muda periodicamente
     */
    @Benchmark
    @OperationsPerInvocation(1000)
    public void traditionalWithChanges(BenchmarkState state) {
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                state.traditionalService.setFlag("new_payment", i % 200 == 0);
            }
            state.traditionalProcessor.process(100.0);
        }
    }

    /**
     * Benchmark com mudança frequente de flag (NOP)
     */
    @Benchmark
    @OperationsPerInvocation(1000)
    public void nopWithChanges(BenchmarkState state) {
        FeatureFlagRegistry registry = FeatureFlagRegistry.getInstance();
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                registry.setFlagState("new_payment_nop", i % 200 == 0);
            }
            state.nopProcessor.process(100.0);
        }
    }
}
