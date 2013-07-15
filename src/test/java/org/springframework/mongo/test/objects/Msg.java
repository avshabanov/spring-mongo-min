package org.springframework.mongo.test.objects;

import java.net.URI;
import java.net.URL;

public final class Msg extends TestDomainObject {
    private String id;
    private MsgState state;
    private URL url;
    private URI uri;

    public Msg() {
    }

    public Msg(String id, MsgState state, URL url, URI uri) {
        this();
        this.id = id;
        this.state = state;
        this.url = url;
        this.uri = uri;
    }

    public Msg(MsgState state, URL url, URI uri) {
        this(null, state, url, uri);
    }

    public Msg(String id, Msg origin) {
        this(id, origin.getState(), origin.getUrl(), origin.getUri());
    }

    public String getId() {
        return id;
    }

    public MsgState getState() {
        return state;
    }

    public URL getUrl() {
        return url;
    }

    public URI getUri() {
        return uri;
    }
}
