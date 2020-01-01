package org.ka.junkins.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoint {
    private final String fullPath;
    private final String path;

    public static Endpoint create(String path) {
        return new Endpoint(path, path);
    }

    public static Endpoint create(Endpoint parent, String path) {
        return new Endpoint(parent.fullPath + path, path);
    }
}
