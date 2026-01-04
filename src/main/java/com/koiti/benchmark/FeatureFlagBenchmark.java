package com.koiti.benchmark;

import com.koiti.nop.feature.FeatureFlag;
import com.koiti.nop.feature.FeatureFlagRegistry;
import com.koiti.nop.payment.NOPPaymentProcessor;
import com.koiti.traditional.TraditionalFeatureFlagService;
import com.koiti.traditional.TraditionalPaymentProcessor;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparando abordagem Tradicional vs NOP
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

    private static final String TRADITIONAL_FLAG_NAME = "payment-v2";
    private static final String NOP_FLAG_NAME = "payment-v2-nop";

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        TraditionalFeatureFlagService traditionalService;
        TraditionalPaymentProcessor traditionalProcessor;

        NOPPaymentProcessor nopProcessor;
        FeatureFlagRegistry nopRegistry;
        FeatureFlag nopFlag;

        @Setup(Level.Trial)
        public void setup() {
            // Setup Tradicional
            traditionalService = new TraditionalFeatureFlagService();
            traditionalService.setFlag(TRADITIONAL_FLAG_NAME, true);
            traditionalProcessor = new TraditionalPaymentProcessor(traditionalService);

            // Setup NOP
            nopRegistry = FeatureFlagRegistry.getInstance();
            nopRegistry.clear();

            nopRegistry.createOrUpdate(NOP_FLAG_NAME, true);
            nopFlag = nopRegistry.getFlag(NOP_FLAG_NAME);
            nopProcessor = new NOPPaymentProcessor(NOP_FLAG_NAME);

            if (nopFlag == null) {
                throw new IllegalStateException("NOP flag não foi criada corretamente!");
            }
        }
    }

    /**
     * Benchmark da abordagem TRADICIONAL
     * - Verifica o flag a cada chamada
     * - Overhead de if + branch prediction
     */
    @Benchmark
    @Threads(1)
    public void traditional_1thread(BenchmarkState state, Blackhole bh) {
        state.traditionalProcessor.process(100.0);
        bh.consume(state.traditionalProcessor);
    }

    /**
     * Benchmark da abordagem NOP
     * - SEM if em runtime
     * - Execução direta
     */
    @Benchmark
    @Threads(1)
    public void nop_1thread(BenchmarkState state, Blackhole bh) {
        state.nopProcessor.process(100.0);
        bh.consume(state.nopProcessor);
    }

    /**
     * Benchmark com mudança frequente de flag (Traditional)
     * Simula cenário onde o flag muda periodicamente
     */
    @Benchmark
    @Threads(1)
    @OperationsPerInvocation(1000)
    public void traditional_1thread_withChanges(BenchmarkState state, Blackhole bh) {
        int operations = 0;
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                // ✅ Usa a constante
                state.traditionalService.setFlag(TRADITIONAL_FLAG_NAME, i % 200 == 0);
            }
            state.traditionalProcessor.process(100.0);
            operations++;
        }
        bh.consume(operations);
    }

    /**
     * Benchmark com mudança frequente de flag (NOP)
     */
    @Benchmark
    @Threads(1)
    @OperationsPerInvocation(1000)
    public void nop_1thread_withChanges(BenchmarkState state, Blackhole bh) {
        int operations = 0;
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                // ✅ Agora nopFlag não é mais null!
                state.nopFlag.setEnabled(i % 200 == 0);
            }
            state.nopProcessor.process(100.0);
            operations++;
        }
        bh.consume(operations);
    }

    @Benchmark
    @Threads(Threads.MAX)
    @OperationsPerInvocation(1000)
    public void traditional_maxthreads_withChanges(BenchmarkState state, Blackhole bh) {
        int operations = 0;
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                state.traditionalService.setFlag("payment-v2", i % 200 == 0);
            }
            state.traditionalProcessor.process(100.0);
            operations++;
        }
        bh.consume(operations);
    }

}