package org.ka.junkins.storage.server;

import dagger.Provides;
import org.ka.junkins.storage.client.Avro;
import org.ka.junkins.storage.client.Build;
import org.ka.junkins.storage.client.BuildStep;
import org.ka.junkins.storage.client.Endpoints;

import javax.inject.Named;
import java.util.function.Consumer;

import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

public class WebModule {
    @Provides static Runnable providesWebApp(Consumer<Build> processBuild,
                                             Consumer<BuildStep> processBuildStep) {
        port(4567);
        path(Endpoints.V1.INGEST, () -> {
            post(Endpoints.V1.POST_BUILD_INFO, (req, resp) -> {
                var build = Avro.from(Build.class, req.raw().getInputStream());
                processBuild.accept(build);
                return "";
            });
            post(Endpoints.V1.POST_BUILD_STEP_INFO, (req, resp) -> {
                var buildStep = Avro.from(BuildStep.class, req.raw().getInputStream());
                processBuildStep.accept(buildStep);
                return "";
            });
        });

        return () -> {};
    }
}
