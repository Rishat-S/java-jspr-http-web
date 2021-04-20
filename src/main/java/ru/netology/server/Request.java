package ru.netology.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream inputStream;

    private Request(String method, String path, Map<String, String> headers, InputStream inputStream) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.inputStream = inputStream;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            throw new IOException("Invalid request");
        }

        var method = parts[0];
        var path = parts[1];
        String line;
        Map<String, String> headers = new ConcurrentHashMap<>();
        while (!(line = in.readLine()).isEmpty()) {
            var i = line.indexOf(":");
            var headerName = line.substring(0, i);
            var headerValue = line.substring(i + 2);
            headers.put(headerName, headerValue);
        }

        return new Request(method, path, headers, inputStream);
    }

    public String getPath() {
        return path;
    }
}
