package com.fpvdrone.registry;

import java.util.function.Supplier;

/** Holds an eagerly registered value; keeps the Forge-era {@code .get()} call sites unchanged. */
public final class RegistryObject<T> implements Supplier<T> {
    private final T value;

    RegistryObject(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return this.value;
    }
}
