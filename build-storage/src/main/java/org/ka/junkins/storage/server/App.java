package org.ka.junkins.storage.server;

import org.ka.junkins.storage.server.cassandra.Cassandra;
import org.ka.junkins.storage.server.cassandra.Mapping;
import org.ka.junkins.storage.server.services.StoreDataServices;

public class App {
    public static void main(String[] args) {
        var cassandra = Cassandra.createEmbedded();
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
