package org.springframework.mongo.test.objects;

/**
 * @author Alexander Shabanov
 */
public final class Book extends TestDomainObject {
    private String name;
    private long pages;

    public Book() {
    }

    public Book(String name, long pages) {
        this();
        this.name = name;
        this.pages = pages;
    }

    public String getName() {
        return name;
    }

    public long getPages() {
        return pages;
    }
}
