package dev.dimlight.test;

import io.vertx.core.Vertx;

/**
 *
 */
public class App {
    public static void main(String[] args) {
        // Create an HTTP server which simply returns "Hello World!" to each request.
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    }}
