package ru.netology;

import ru.netology.server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(64);

        server.listen(9999);

    }
}
