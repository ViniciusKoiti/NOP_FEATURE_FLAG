package com.koiti.nop.feature;

import com.koiti.nop.core.FeatureFlagObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Feature Flag com Paradigma Orientado a Notificações (NOP)
 * 
 * Principais diferenças da abordagem tradicional:
 * 1. Notifica observers quando muda (proativo)
 * 2. Observers reagem à mudança (reativo)
 * 3. Sem verificações repetidas em runtime
 */
public class FeatureFlag {
    private final String name;
    private volatile boolean enabled;
    private final List<FeatureFlagObserver> observers;

    public FeatureFlag(String name, boolean initialState) {
        this.name = name;
        this.enabled = initialState;
        this.observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Adiciona um observer que será notificado de mudanças
     */
    public void addObserver(FeatureFlagObserver observer) {
        observers.add(observer);
        // Notifica o estado atual imediatamente
        observer.onFeatureChanged(name, enabled);
    }

    /**
     * Remove um observer
     */
    public void removeObserver(FeatureFlagObserver observer) {
        observers.remove(observer);
    }

    /**
     * Muda o estado e NOTIFICA todos os observers
     * 
     * NOP: Esta é a ÚNICA vez que a mudança é processada!
     * Não há verificações repetidas depois disso.
     */
    public void setEnabled(boolean newState) {
        if (this.enabled != newState) {
            this.enabled = newState;
            notifyObservers();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    /**
     * Notifica todos os observers sobre a mudança
     * 
     * Conceito NOP: "Gossip útil"
     * A flag "grita" que mudou, quem estiver interessado reage
     */
    private void notifyObservers() {
        for (FeatureFlagObserver observer : observers) {
            observer.onFeatureChanged(name, enabled);
        }
    }
}
