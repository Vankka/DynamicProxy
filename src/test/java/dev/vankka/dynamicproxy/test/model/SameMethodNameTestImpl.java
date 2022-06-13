package dev.vankka.dynamicproxy.test.model;

import java.util.List;

public class SameMethodNameTestImpl implements SameMethodNameTest {

    @Override
    public boolean method(String string) {
        return false;
    }

    @Override
    public boolean method(String[] stringArray) {
        return false;
    }

    @Override
    public boolean method(List<String> stringList) {
        return false;
    }
}
