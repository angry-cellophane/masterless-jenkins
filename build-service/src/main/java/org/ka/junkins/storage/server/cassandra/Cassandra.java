package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.nosan.embedded.cassandra.EmbeddedCassandraBuilder;
import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.artifact.Artifact;

import java.nio.file.Path;
import java.util.function.Supplier;

public class Cassandra {
    public interface Module {
        Runnable stop();
        Supplier<CqlSession> sessionSupplier();
    }

    public static Cassandra.Module createEmbedded(Path pathToDatafiles) {
        var cassandra = new EmbeddedCassandraBuilder()
                .withArtifact(Artifact.ofVersion("4.0-alpha2"))
                .withWorkingDirectory(pathToDatafiles)
                .create();
        cassandra.start();

        var session = new CqlSessionCassandraConnectionFactory().create(cassandra);
        var connection = session.getConnection();

        CqlDataSet.ofClasspaths("cassandra/create_schema.cql").forEachStatement(connection::execute);
        return new Cassandra.Module() {
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
