package org.springframework.mongo.test.objects;

/**
 * @author Alexander Shabanov
 */
public final class Profile extends TestDomainObject {
    private String id;
    private String name;
    private Integer age;

    // for mappable mongo template
    public Profile() {
    }

    public Profile(String id, String name, Integer age) {
        this();
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Profile(String name, Integer age) {
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

    public Integer getAge() {
        return age;
    }
}
