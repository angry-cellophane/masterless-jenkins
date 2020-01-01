package org.ka.junkins.storage.server;

import org.ka.junkins.storage.server.cassandra.Cassandra;
import org.ka.junkins.storage.server.cassandra.Mapping;
import org.ka.junkins.storage.server.services.StoreDataServices;

import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        var cassandraDir = Paths.get("/tmp/cassandra");
        var cassandra = Cassandra.createEmbedded(cassandraDir);
        var mapping = Mapping.create(cassandra);

        var storeServices = StoreDataServices.create(mapping);
        var web = WebApi.create(storeServices);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            web.stop();
            cassandra.stop();
        }));

        web.start();
    }
}
