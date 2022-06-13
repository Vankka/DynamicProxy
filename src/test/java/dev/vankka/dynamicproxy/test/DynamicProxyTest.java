package dev.vankka.dynamicproxy.test;

import dev.vankka.dynamicproxy.test.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicProxyTest {

    @Test
    public void interfaceTest() {
        InterfaceTestDynamicProxy proxy = new InterfaceTestDynamicProxy();
        InterfaceTest interfaceTest = proxy.getProxy(new InterfaceTestImpl());
        assertTrue(interfaceTest.first());
        assertFalse(interfaceTest.second());
        assertFalse(interfaceTest.third());
        assertTrue(interfaceTest.fourth());
    }

    @Test
    public void sameMethodNameTest() {
        SameMethodNameTestDynamicProxy proxy = new SameMethodNameTestDynamicProxy();
        SameMethodNameTest sameMethodNameTest = proxy.getProxy(new SameMethodNameTestImpl());
        assertTrue(sameMethodNameTest.method((String) null));
        assertFalse(sameMethodNameTest.method((String[]) null));
        assertFalse(sameMethodNameTest.method((List<String>) null));
    }

    @Test
    public void originalTest() {
        new OriginalTestProxy(new InterfaceTestImpl()).getProxy();
    }

    @Test
    public void subclassTest() {
        //TopClassDynamicProxy proxy = new TopClassDynamicProxy();

    }
}
