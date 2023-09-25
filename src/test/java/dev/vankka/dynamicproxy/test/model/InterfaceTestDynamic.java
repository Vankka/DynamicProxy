package dev.vankka.dynamicproxy.test.model;

import dev.vankka.dynamicproxy.processor.Original;
import dev.vankka.dynamicproxy.processor.Proxy;

@Proxy(InterfaceTest.class)
public abstract class InterfaceTestDynamic implements InterfaceTest {

    @Original
    private final InterfaceTest original;

    public InterfaceTestDynamic(InterfaceTest original) {
        super();
        this.original = original;
    }

    @Override
    public boolean first() {
        return true;
    }

    @Override
    public boolean third() {
        return original.third();
    }

    @Override
    public boolean fourth() {
        return true;
    }
}
