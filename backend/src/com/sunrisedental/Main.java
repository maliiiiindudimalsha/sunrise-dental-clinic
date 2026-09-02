package com.sunrisedental;

import com.sun.net.httpserver.HttpServer;
import com.sunrisedental.controller.LoginController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 404 දෝෂය නිවැරදි කරන ප්‍රධාන පේළිය (Login endpoint එක සම්බන්ධ කිරීම)
        server.createContext("/login", new LoginController());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }
}