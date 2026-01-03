package com.koiti.nop.feature;

import com.koiti.nop.core.FeatureFlagObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry centralizado de Feature Flags
 * 
 * Gerencia todas as flags e facilita a inscrição de observers
 */
public class FeatureFlagRegistry {
    private static final FeatureFlagRegistry INSTANCE = new FeatureFlagRegistry();
    private final Map<String, FeatureFlag> flags = new ConcurrentHashMap<>();

    private FeatureFlagRegistry() {}

    public static FeatureFlagRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Cria ou atualiza uma feature flag
     */
    public FeatureFlag createOrUpdate(String name, boolean enabled) {
        return flags.compute(name, (key, existing) -> {
            if (existing == null) {
                return new FeatureFlag(name, enabled);
            } else {
                existing.setEnabled(enabled);
                return existing;
            }
        });
    }

    /**
     * Atualiza o estado sem recriar a flag
     */
    public void setFlagState(String name, boolean enabled) {
        flags.computeIfAbsent(name, k -> new FeatureFlag(name, enabled))
            .setEnabled(enabled);
    }

    /**
     * Obtém uma feature flag existente
     */
    public FeatureFlag getFlag(String name) {
        return flags.get(name);
    }

    /**
     * Adiciona um observer a uma feature específica
     */
    public void observe(String featureName, FeatureFlagObserver observer) {
        FeatureFlag flag = flags.get(featureName);
        if (flag != null) {
            flag.addObserver(observer);
        }
    }

    /**
     * Limpa todas as flags (útil para testes)
     */
    public void clear() {
        flags.clear();
    }
}
