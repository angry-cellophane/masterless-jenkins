package org.ka.junkins.storage.server.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.github.nosan.embedded.cassandra.EmbeddedCassandraBuilder
import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet
import com.github.nosan.embedded.cassandra.artifact.Artifact
import groovy.transform.CompileStatic

import java.util.function.Supplier

@CompileStatic
class EmbeddedCassandra {
    static Cassandra.Module create() {
        def cassandra = new EmbeddedCassandraBuilder()
                .withArtifact(Artifact.ofVersion("4.0-alpha2"))
                .create()
        cassandra.start()

        def session = new CqlSessionCassandraConnectionFactory().create(cassandra)
        def connection = session.getConnection()

        CqlDataSet.ofClasspaths("cassandra/create_schema.cql").forEachStatement(connection.&execute)
        return new Cassandra.Module() {
            @Override
            Runnable stop() {
                return {
                    connection.close()
                    cassandra.stop()
                } as Runnable
            }

            @Override
            Supplier<CqlSession> sessionSupplier() {
                return { connection } as Supplier<CqlSession>
            }
        }
    }
}
