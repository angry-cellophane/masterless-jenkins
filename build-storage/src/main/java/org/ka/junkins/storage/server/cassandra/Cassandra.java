package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.nosan.embedded.cassandra.EmbeddedCassandraBuilder;
import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.artifact.Artifact;

import java.util.function.Supplier;

public class Cassandra {
    public interface Module {
        Runnable stop();
        Supplier<CqlSession> sessionSupplier();
    }

    public static Module createEmbedded() {
        var cassandra = new EmbeddedCassandraBuilder()
                .withArtifact(Artifact.ofVersion("4.0-alpha2"))
                .create();
        cassandra.start();

        var session = new CqlSessionCassandraConnectionFactory().create(cassandra);
        var connection = session.getConnection();

        return new Module() {
            @Override
            public Runnable stop() {
                return () -> {
                    connection.close();
                    cassandra.stop();
                };
            }

            @Override
            public Supplier<CqlSession> sessionSupplier() {
                return () -> connection;
            }
        };
    }


}
