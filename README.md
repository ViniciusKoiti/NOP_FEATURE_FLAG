# Feature Flags with Notification-Oriented Paradigm (NOP) / Feature Flags com Paradigma Orientado a Notificações (NOP)

## Overview / Visão Geral
- EN: Apply **Notification Oriented Paradigm (NOP)** in Java to remove repeated feature-flag checks on hot paths.
- PT: Aplica o **Paradigma Orientado a Notificações (NOP)** em Java para remover verificações repetidas de feature flags em rotas quentes.

## Problem / Problema
Traditional approach (checks every call / checa toda chamada):

```java
public void process(double amount) {
    if (featureFlagService.isEnabled("new_payment")) {
        newPaymentProcessor.process(amount);
    } else {
        legacyPaymentProcessor.process(amount);
    }
}
```

- EN: Redundant work, CPU overhead, energy waste, branch misses.
- PT: Trabalho redundante, overhead de CPU, desperdício de energia, branch misses.

## Solution: NOP in Java / Solução: NOP em Java
1) EN: Flags notify when they change (events). PT: Flags notificam quando mudam (eventos).
2) EN: Components react to the change (observers). PT: Componentes reagem à mudança (observers).
3) EN: Hot path runs without `if`. PT: Caminho quente executa sem `if`.

## Project Structure / Estrutura do Projeto
```
src/
├─ traditional/        # Baseline tradicional
├─ nop/                # Implementação NOP
│  ├─ core/            # Infra NOP
│  ├─ feature/         # Flags e registry
│  └─ payment/         # Exemplo de uso
└─ benchmark/          # Benchmarks (JMH)
```

## How it works / Como funciona
- EN: `FeatureFlag` notifies observers on change; `FeatureFlagRegistry` centralizes; processors swap strategy on notification and run without `if`.
- PT: `FeatureFlag` notifica observers ao mudar; `FeatureFlagRegistry` centraliza; processadores trocam estratégia na notificação e executam sem `if`.

## Build & Run / Compilar & Executar
```bash
# Build
mvn clean package

# Demo app
java -cp target/feature-flag-nop-1.0.jar com.koiti.Main

# Benchmarks (throughput + GC)
java -jar target/feature-flag-nop-1.0-benchmarks.jar FeatureFlagBenchmark -prof gc
```

## Recent Benchmark Highlights / Destaques Recentes de Benchmark
- EN: NOP (stable): ~1.13M ops/ms; alloc ~0.006 MB/s; GC 0.
  PT: NOP (estável): ~1,13M ops/ms; aloc. ~0,006 MB/s; GC 0.
- EN: NOP with frequent changes: ~0.77M ops/ms; ~0.24 B/op; 175 MB/s due to very high throughput; 16 GCs (18 ms total).
  PT: NOP com mudanças frequentes: ~0,77M ops/ms; ~0,24 B/op; 175 MB/s pelo throughput altíssimo; 16 GCs (18 ms no total).
- EN: Traditional: ~0.22M ops/ms; ~0.006–0.007 MB/s; GC 0.
  PT: Tradicional: ~0,22M ops/ms; ~0,006–0,007 MB/s; GC 0.

Interpretation / Interpretação: o MB/s alto no cenário “with changes” reflete o volume extremo de operações; por operação a alocação é mínima (~0,24 byte). Em cargas reais, o impacto tende a ser baixo frente ao ganho de CPU.

## Applied Concepts / Conceitos Aplicados
- Observer Pattern — mudança de estado notifica interessados.
- Strategy Pattern — troca de implementação sob demanda.
- Event-Driven Architecture — reação a eventos, não polling.
- Cache Warming / upfront decision — decisão no momento da mudança do flag, não no hot path.

## References / Referências
- [Are Feature Flags Bullsh*t?](https://dev.to/matheuscamarques/are-feature-flags-bullsht-why-your-if-is-killing-performance-and-the-planet-26bi)
- SIMÃO, Jean Marcelo. *Paradigma Orientado a Notificações*
- NEGRINI, Fabio. *NOPL Erlang-Elixir Technology*
