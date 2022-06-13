package dev.vankka.dynamicproxy.test.model;

import java.util.List;

public interface SameMethodNameTest {

    boolean method(String string);
    boolean method(String[] stringArray);
    boolean method(List<String> stringList);
}
