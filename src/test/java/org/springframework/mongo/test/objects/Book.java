package org.springframework.mongo.test.objects;

/**
 * @author Alexander Shabanov
 */
public final class Book extends TestDomainObject {
    private String name;
    private int pages;

    public Book() {
    }

    public Book(String name, int pages) {
        this();
        this.name = name;
        this.pages = pages;
    }

    public String getName() {
        return name;
    }

    public int getPages() {
        return pages;
    }
}
