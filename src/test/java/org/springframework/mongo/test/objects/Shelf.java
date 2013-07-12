package org.springframework.mongo.test.objects;

import java.util.List;

/**
 * @author Alexander Shabanov
 */
public final class Shelf extends TestDomainObject {
    private String id;
    private List<Book> books;
    private String label;

    public Shelf() {
    }

    public Shelf(String id, List<Book> books, String label) {
        this();
        this.id = id;
        this.books = books;
        this.label = label;
    }

    public Shelf(String id, Shelf origin) {
        this(id, origin.getBooks(), origin.getLabel());
    }

    public Shelf(List<Book> books, String label) {
        this(null, books, label);
    }

    public String getId() {
        return id;
    }

    public List<Book> getBooks() {
        return books;
    }

    public String getLabel() {
        return label;
    }
}
