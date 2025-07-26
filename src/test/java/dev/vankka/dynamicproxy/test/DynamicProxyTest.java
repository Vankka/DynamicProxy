/*
 * MIT License
 *
 * Copyright (c) 2022-2025 Henri "Vankka" Schubin
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

package dev.vankka.dynamicproxy.test;

import dev.vankka.dynamicproxy.test.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicProxyTest {

    @Test
    public void interfaceTest() {
        InterfaceTestDynamicProxy proxy = new InterfaceTestDynamicProxy(new InterfaceTestImpl());
        InterfaceTest interfaceTest = proxy.getProxy();
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
