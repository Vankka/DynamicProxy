/*
 * MIT License
 *
 * Copyright (c) 2022-2024 Henri "Vankka" Schubin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        getAllInterfaces(original.getClass(), interfaces);

        return Proxy.newProxyInstance(
                originalClass.getClassLoader(),
                interfaces.toArray(new Class<?>[0]),
                new Handler(original, proxy)
        );
    }

    private void getAllInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        while (clazz != null) {
            for (Class<?> theInterface : clazz.getInterfaces()) {
                if (interfaces.add(theInterface)) {
                    getAllInterfaces(theInterface, interfaces);
                }
            }

            clazz = clazz.getSuperclass();
        }
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

                    return invokeMethod(proxyMethod, args);
                }
            }

            return method.invoke(original, args);
        }

        private Object invokeMethod(Method proxyMethod, Object[] args) {
            try {
                proxyMethod.setAccessible(true);
                return proxyMethod.invoke(proxy, args);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new InvocationError(e);
            }
        }
    }

}
