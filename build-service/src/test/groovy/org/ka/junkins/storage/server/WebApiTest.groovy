package org.ka.junkins.storage.server

import org.ka.junkins.storage.client.Build
import org.ka.junkins.storage.client.BuildStatus
import org.ka.junkins.storage.client.BuildStep
import org.ka.junkins.storage.client.Clients
import org.ka.junkins.storage.client.StepResult
import org.ka.junkins.storage.server.services.StoreDataServices
import spark.Spark
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Consumer

class WebApiTest extends Specification {

    static class Services implements StoreDataServices.Module {

        final List<Build> builds = new ArrayList<>()
        final List<BuildStep> buildSteps = new ArrayList<>()

        @Override
        Consumer<Build> processBuild() {
            return { build -> builds.add(build) }
        }

        @Override
        Consumer<BuildStep> processBuildStep() {
            return { buildStep -> buildSteps.add(buildStep) }
        }
    }

    @Shared WebApi.Module web
    @Shared Services services
    @Shared Clients clients

    void setupSpec() {
        services = new Services()
        web = WebApi.create(services)
        web.start()
        clients = Clients.create("http://localhost:${Spark.port()}")
    }

    void cleanupSpec() {
        web?.stop()
    }

    void cleanup() {
        services?.builds?.clear()
        services?.buildSteps?.clear()
    }

    void 'build decoded successfully'() {
        given:
        def build = Build.newBuilder()
                .setName('build#1')
                .setBuildId('id#1')
                .setJobId('job#1')
                .setNumber(1)
                .setStatus(BuildStatus.RUNNING)
                .build()

        when:
        clients.of(Build).submit(build)
        then:
        services.builds.size() == 1
        services.builds.first() == build
    }

    void 'buildstep decoded successfully'() {
        given:
        def step = BuildStep.newBuilder()
                .setName('build#1')
                .setBuildId('id')
                .setStepId(1)
                .setParentStepId(0)
                .setResult(StepResult.SUCCEEDED)
                .setLog(['log1', 'log2'])
                .build()

        when:
        clients.of(BuildStep).submit(step)
        then:
        services.buildSteps.size() == 1
        services.buildSteps.first() == step
    }
}
