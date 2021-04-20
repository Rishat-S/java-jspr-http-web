package ru.netology.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    //    List<String> validPaths = List.of(
//            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
//            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
//            "/events.html", "/events.js"
//    );
    ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private final Handler notFoundHandler = (request, out) -> {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (Exception e) {
            // FIXME:
        }

    };

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.get(method) == null) {
            handlers.put(method, new ConcurrentHashMap<>());
        }

        handlers.get(method).put(path, handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> Server.this.processConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processConnection(Socket socket) {
        try (
                socket;
                final var in = socket.getInputStream();
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            var request = Request.fromInputStream(in);

            var handlerMap = handlers.get(request.getMethod());
            if (handlerMap == null) {
                notFoundHandler.handle(request, out);
                return;
            }

            var handler = handlerMap.get(request.getPath());
            if (handler == null) {
                notFoundHandler.handle(request, out);
                return;
            }

            handler.handle(request, out);

//            final var path = request.getPath();
//            if (!validPaths.contains(path)) {
//                out.write((
//                        "HTTP/1.1 404 Not Found\r\n" +
//                                "Content-Length: 0\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.flush();
//                return;
//            }
//
//            final var filePath = Path.of(".", "public", path);
//            final var mimeType = Files.probeContentType(filePath);
//
//            if (path.equals("/classic.html")) {
//                final var template = Files.readString(filePath);
//                final var content = template.replace(
//                        "{time}",
//                        LocalDateTime.now().toString()
//                ).getBytes();
//                out.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Type: " + mimeType + "\r\n" +
//                                "Content-Length: " + content.length + "\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.write(content);
//                out.flush();
//                return;
//            }
//
//            final var length = Files.size(filePath);
//            out.write((
//                    "HTTP/1.1 200 OK\r\n" +
//                            "Content-Type: " + mimeType + "\r\n" +
//                            "Content-Length: " + length + "\r\n" +
//                            "Connection: close\r\n" +
//                            "\r\n"
//            ).getBytes());
//            Files.copy(filePath, out);
//            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
