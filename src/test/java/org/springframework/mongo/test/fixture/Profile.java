package org.springframework.mongo.test.fixture;

/**
 * @author Alexander Shabanov
 */
public class Profile extends TestDomainObject {
    private String id;
    private String name;
    private int age;

    public Profile(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Profile(String name, int age) {
        this(null, name, age);
    }

    public Profile(String id, Profile origin) {
        this(id, origin.getName(), origin.getAge());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
