package dev.vankka.dynamicproxy.test.model;

import dev.vankka.dynamicproxy.CallOriginal;
import dev.vankka.dynamicproxy.processor.Proxy;

@Proxy(InterfaceTest.class)
public abstract class InterfaceTestDynamic implements InterfaceTest {

    public InterfaceTestDynamic() {
        super();
    }

    @Override
    public boolean first() {
        return true;
    }

    @Override
    public boolean third() {
        return CallOriginal.call();
    }

    @Override
    public boolean fourth() {
        CallOriginal.call();
        return true;
    }
}
