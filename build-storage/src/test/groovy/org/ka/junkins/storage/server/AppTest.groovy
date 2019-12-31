package org.ka.junkins.storage.server

import org.ka.junkins.storage.client.Build
import org.ka.junkins.storage.client.BuildStatus
import org.ka.junkins.storage.client.ClientException
import org.ka.junkins.storage.client.Clients
import org.ka.junkins.storage.server.cassandra.Cassandra
import org.ka.junkins.storage.server.cassandra.Mapping
import org.ka.junkins.storage.server.services.StoreDataServices
import spark.Spark
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
class AppTest extends Specification {
    @Shared Cassandra.Module cassandra
    @Shared WebApi.Module web
    @Shared Clients clients

    void setupSpec() {
        def cassandra = Cassandra.createEmbedded()
        def mapping = Mapping.create(cassandra)

        def storeServices = StoreDataServices.create(mapping)
        def web = WebApi.create(storeServices)

        web.start()

        clients = Clients.create("http://localhost:${Spark.port()}")
    }

    void cleanupSpec() {
        web?.stop()
        cassandra?.stop()
    }

    void 'submit a not started build'() {
        given:
        def build = Build.newBuilder()
                        .setName('build#1')
                        .setId('id#1')
                        .setNumber(1)
                        .setStatus(BuildStatus.NOT_STARTED)
                        .build()

        when:
        clients.of(Build).submit(build)
        then:
        notThrown(ClientException)
    }
}
