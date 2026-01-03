# Feature Flags — NOP vs Tradicional (JMH)

## Comando usado
```
java -jar target/feature-flag-nop-1.0-benchmarks.jar FeatureFlagBenchmark -prof gc
```

## Resultados (thrpt, ops/ms) e GC
- nopApproach: 1 007 891 ops/ms (±300k); alloc 0.006 MB/s; GC 0.
- nopWithChanges: 696 635 ops/ms (±275k); alloc 159 MB/s; GC 5 (5 ms).
- traditionalApproach: 204 797 ops/ms (±52k); alloc 0.007 MB/s; GC 0.
- traditionalWithChanges: 202 975 ops/ms (±49k); alloc 0.006 MB/s; GC 0.

## Resultados atualizados (thrpt, ops/ms, 16 amostras)
- nopApproach: 1 133 700 ops/ms (±21 950); alloc 0.006 MB/s; GC 0.
- nopWithChanges: 768 939 ops/ms (±33 763); alloc 175.9 MB/s; GC 16 (18 ms).
- traditionalApproach: 218 182 ops/ms (±16 522); alloc 0.006 MB/s; GC 0.
- traditionalWithChanges: 218 565 ops/ms (±8 521); alloc 0.006 MB/s; GC 0.

## Leitura
- Performance: NOP entrega ~5× a ~10× o throughput do tradicional.
- Memória: só nopWithChanges gera alocação/GC perceptível; demais ~zero.
- Variância: erro alto nos cenários NOP; aumente warmup/iterações/forks para estabilizar.

### Nota sobre alocação do NOP com mudanças
- O `alloc.rate.norm` caiu de 10 B/op para ~0,24 B/op (cada operação quase não aloca). 
- O valor em MB/s é alto porque o throughput também é altíssimo (~7,7e8 ops/s): 0,24 B * 7,7e8 ops/s ≈ 184 MB/s.
- Para reduzir ainda mais: evitar qualquer objeto transitório dentro do loop de mudança (já usamos `setFlagState`); o próximo passo seria remover alocações dos lambdas/boxing no benchmark ou baixar o throughput simulando carga realista.

## Hipótese para custo de memória em nopWithChanges
- `createOrUpdate` recria/atualiza flag e dispara notificações, alocando a cada troca.
- No tradicional, a mudança só altera o map; o caminho de execução só faz “if”.

## Ajustes sugeridos (memória)
- Criar a flag uma vez e mudar estado com `setEnabled`:
  - `FeatureFlag flag = registry.createOrUpdate("new_payment_nop", true);`
  - depois: `flag.setEnabled(...)` ou `registry.getFlag(...).setEnabled(...)`.
- Strategies já são reutilizadas; inicializar `currentStrategy` com fallback para evitar null.
- Muitas inscrições? Considerar `ConcurrentHashMap` de observers em vez de `CopyOnWriteArrayList`.

## Próximos passos de medição
- Rodada mais estável:
  - `java -jar target/feature-flag-nop-1.0-benchmarks.jar FeatureFlagBenchmark -prof gc -wi 5 -i 8 -f 2`
- Focar no caso crítico:
  - `java -jar target/feature-flag-nop-1.0-benchmarks.jar FeatureFlagBenchmark.nopWithChanges -prof gc -wi 5 -i 8 -f 2`
- Mais detalhe de alocação: JFR (`-XX:StartFlightRecording`) ou `-prof perfasm` (se perf disponível).
