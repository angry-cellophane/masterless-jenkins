package org.ka.junkins.storage.client

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.ka.junkins.avro.Avro
import spock.lang.Shared
import spock.lang.Specification

class ClientTest extends Specification {

    @Shared MockWebServer server
    @Shared Clients clients
    @Shared Endpoints.V1 api

    void setupSpec() {
        server = new MockWebServer()
        server.start()

        clients = Clients.create(server.url('').url)
        api = Endpoints.V1.from('')
    }

    void cleanupSpec() {
        server.shutdown()
    }

    void 'send valid build'() {
        given:
        def build = Build.newBuilder()
                .setId('id#1')
                .setName('build#1')
                .setNumber(1)
                .setResult(BuildResult.SUCCEEDED)
                .setStatus(BuildStatus.COMPLETED)
                .build()

        when:
        server.enqueue(new MockResponse().setResponseCode(200))
        clients.of(Build).submit(build)

        then:
        def request = server.takeRequest()
        request.getPath() == api.POST_BUILD
        request.getMethod() == 'POST'

        when:
        def received = Avro.from(Build, request.body.readByteArray())

        then:
        received == build
    }

    void 'default value for result is set'() {
        given:
        def build = Build.newBuilder()
                .setId('id#1')
                .setName('build#1')
                .setNumber(1)
                .setStatus(BuildStatus.COMPLETED)
                .build()

        when:
        server.enqueue(new MockResponse().setResponseCode(200))
        clients.of(Build).submit(build)

        then:
        def request = server.takeRequest()
        request.getPath() == api.POST_BUILD
        request.getMethod() == 'POST'

        when:
        def received = Avro.from(Build, request.body.readByteArray())

        then:
        received == build
    }

    void 'send valid build step'() {
        given:
        def step = BuildStep.newBuilder()
                .setBuildId("buildId")
                .setName("shell")
                .setStepId(1)
                .setParentStepId(2)
                .setResult(StepResult.SUCCEEDED)
                .setLog(['started', 'done'])
                .build()

        when:
        server.enqueue(new MockResponse().setResponseCode(200))
        clients.of(BuildStep).submit(step)

        then:
        def request = server.takeRequest()
        request.getPath() == api.POST_BUILD_STEP
        request.getMethod() == 'POST'

        when:
        def received = Avro.from(BuildStep, request.body.readByteArray())

        then:
        received == step
    }
}