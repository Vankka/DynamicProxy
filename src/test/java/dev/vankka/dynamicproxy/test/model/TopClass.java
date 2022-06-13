package dev.vankka.dynamicproxy.test.model;

import dev.vankka.dynamicproxy.processor.Proxy;

public class TopClass {

    public interface Interface {
        void method();
    }

    @Proxy(Interface.class)
    public static abstract class TopClassDynamic implements Interface {

        @Override
        public void method() {

        }
    }
}
