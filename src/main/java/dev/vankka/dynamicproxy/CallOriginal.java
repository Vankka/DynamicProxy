package dev.vankka.dynamicproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CallOriginal {

    private CallOriginal() {}

    static final ThreadLocal<Entry> METHOD = new ThreadLocal<>();

    static class Entry {

        private final Method method;
        private final Object object;

        Entry(Method method, Object object) {
            this.method = method;
            this.object = object;
        }
    }

    /**
     * Calls the original method, can be used within a {@link dev.vankka.dynamicproxy.processor.Proxy} proxies method.
     * @param arguments the arguments for the method
     * @return the value the method returns
     * @param <T> the return type
     */
    @SuppressWarnings("unchecked")
    public static <T> T call(Object... arguments) {
        Entry pair = METHOD.get();
        if (pair == null) {
            throw new IllegalArgumentException("Not in proxy method.");
        }

        try {
            Method method = pair.method;
            method.setAccessible(true);
            return (T) method.invoke(pair.object, arguments);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new InvocationError(e.getCause());
        }
    }
}
