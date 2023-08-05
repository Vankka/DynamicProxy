# DynamicProxy

[![Maven Central](https://img.shields.io/maven-central/v/dev.vankka/dynamicproxy?label=release)](https://central.sonatype.com/search?q=g:dev.vankka%20a:dynamicproxy)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dev.vankka/dynamicproxy?label=dev&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/#view-repositories;snapshots~browsestorage~dev)

Library to help with dynamically proxying Java interfaces

## Dependency

⚠️ Dependency info & additional build tool information is WIP

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'dev.vankka:dynamicproxy:1.0.0'
    runtimeOnly 'dev.vankka:dynamicproxy:1.0.0:runtime'
    annotationProcessor 'dev.vankka:dynamicproxy:1.0.0'
}
```

## Example

### Interface
```java
interface Person {
    
    String getName();
    int getAge();
    void remove();
}
```

### Proxy template
A `abstract` class annotated with `@Proxy` implementing the desired interface 
```java
@Proxy(Person.class)
public abstract class PersonDynamic implements Person {
    
    @Override
    public int getAge() {
        return CallOriginal.call() + 1;
    }
}
```

### Using the (generated) proxy
```java
public class Test {
    public static void proxyTest() {
        Person originalPerson = new PersonImpl();
        Person proxyPerson = new PersonDynamicProxy().getProxy(originalPerson);
        
        System.out.println(originalPerson.getAge()); // 20
        System.out.println(proxyPerson.getAge()); // 21
    }
}
```

## Using the `@Original` annotation
If the `@Original` is set on a field of the proxy template class, it will be used for the proxy created by `getProxy`, example below

```java
@Proxy(Person.class)
public abstract class PersonDynamic implements Person {
    
    @Original
    private final Person person;
    
    private final String lastName;
    
    public PersonDynamic(Person person, String lastName) {
        this.person = person;
        this.lastName = lastName;
    }
    
    @Override
    public String getName() {
        return person.getName() + " " + lastName;
    }
}
```

```java
public class Test {
    public static void proxyTest() {
        Person originalPerson = new PersonImpl();
        Person proxyPerson = new PersonDynamicProxy(originalPerson, "Smith").getProxy();
        
        System.out.println(originalPerson.getName()); // John
        System.out.println(proxyPerson.getName()); // John Smith
    }
}
```
