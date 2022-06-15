package dev.vankka.dynamicproxy;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class DynamicProxy {

    private final Map<String, List<Method>> methods = new HashMap<>();

    public DynamicProxy(@NotNull Class<?> proxyType) {
        for (Method method : proxyType.getDeclaredMethods()) {
            methods.computeIfAbsent(method.getName(), key -> new ArrayList<>()).add(method);
        }
    }

    @SuppressWarnings("unused") // Used by generated classes
    @NotNull
    public Object make(@NotNull Object original, @NotNull Object proxy) {
        Class<?> originalClass = original.getClass();
        return Proxy.newProxyInstance(
                originalClass.getClassLoader(),
                originalClass.getInterfaces(),
                new Handler(original, proxy)
        );
    }

    private class Handler implements InvocationHandler {

        private final Object original;
        private final Object proxy;

        public Handler(Object original, Object proxy) {
            this.original = original;
            this.proxy = proxy;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            List<Method> methodList = methods.get(method.getName());
            if (methodList != null) {
                int parameterCount = method.getParameterCount();
                Class<?>[] parameterTypes = method.getParameterTypes();
                for (Method proxyMethod : methodList) {
                    if (proxyMethod.getParameterCount() != parameterCount) {
                        continue;
                    }
                    if (!Arrays.equals(proxyMethod.getParameterTypes(), parameterTypes)) {
                        continue;
                    }

                    return invokeMethod(proxyMethod, method, args);
                }
            }

            return method.invoke(original, args);
        }

        private Object invokeMethod(Method proxyMethod, Method method, Object[] args) {
            try {
                CallOriginal.METHOD.set(new CallOriginal.Entry(method, original));
                proxyMethod.setAccessible(true);
                return proxyMethod.invoke(proxy, args);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new InvocationError(e);
            } finally {
                CallOriginal.METHOD.remove();
            }
        }
    }

}
