package com.koiti.nop.core;

/**
 * Observer para mudanças em Feature Flags
 * 
 * Conceito NOP: Entidades NOTIFICAM mudanças
 * ao invés de serem constantemente consultadas
 */
@FunctionalInterface
public interface FeatureFlagObserver {
    /**
     * Notificado quando o estado da feature muda
     * 
     * @param featureName nome da feature
     * @param enabled novo estado
     */
    void onFeatureChanged(String featureName, boolean enabled);
}
