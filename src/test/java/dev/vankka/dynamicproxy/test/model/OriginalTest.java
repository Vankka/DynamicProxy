package dev.vankka.dynamicproxy.test.model;

import dev.vankka.dynamicproxy.processor.Original;
import dev.vankka.dynamicproxy.processor.Proxy;

@Proxy(InterfaceTest.class)
public abstract class OriginalTest implements InterfaceTest {

    @Original

            ()
    private final InterfaceTest original;

    public OriginalTest(InterfaceTest original) {
        this.original = original;
    }

}
