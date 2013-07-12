package org.springframework.mongo.test.objects;

import java.util.List;

/**
 * @author Alexander Shabanov
 */
public final class Shelf extends TestDomainObject {
    private String id;
    private List<Book> books;
    private List<String> tags;
    private Profile librarian;

    public Shelf() {
    }

    public Shelf(String id, List<Book> books, List<String> tags, Profile librarian) {
        this();
        this.id = id;
        this.books = books;
        this.tags = tags;
        this.librarian = librarian;
    }

    public Shelf(String id, Shelf origin) {
        this(id, origin.getBooks(), origin.getTags(), origin.getLibrarian());
    }

    public Shelf(List<Book> books, List<String> tags, Profile librarian) {
        this(null, books, tags, librarian);
    }

    public String getId() {
        return id;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<String> getTags() {
        return tags;
    }

    public Profile getLibrarian() {
        return librarian;
    }
}
