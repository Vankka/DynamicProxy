package dev.vankka.dynamicproxy.test.model;

import dev.vankka.dynamicproxy.processor.Proxy;

@Proxy(SameMethodNameTest.class)
public abstract class SameMethodNameTestDynamic implements SameMethodNameTest {

    @Override
    public boolean method(String string) {
        return true;
    }
}
