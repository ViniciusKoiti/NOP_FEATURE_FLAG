package com.koiti.traditional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação TRADICIONAL de Feature Flags
 * 
 * PROBLEMA: Verifica a condição a CADA chamada
 * - Redundância temporal
 * - Overhead de CPU
 * - Branch prediction failures
 */
public class TraditionalFeatureFlagService {
    private final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    public void setFlag(String name, boolean enabled) {
        flags.put(name, enabled);
    }

    public boolean isEnabled(String name) {
        // ❌ Esta verificação acontece MILHÕES de vezes
        // Mesmo quando o valor não mudou!
        return flags.getOrDefault(name, false);
    }
}
