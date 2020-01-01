package org.ka.junkins.storage.server;

import org.ka.junkins.avro.Avro;
import org.ka.junkins.storage.client.Build;
import org.ka.junkins.storage.client.BuildStep;
import org.ka.junkins.storage.client.Endpoints;
import org.ka.junkins.storage.server.services.StoreDataServices;
import spark.Spark;

import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

public class WebApi {
    public interface Module {
        void start();
        void stop();
    }

    static Module create(StoreDataServices.Module services) {
        var processBuild = services.processBuild();
        var processBuildStep = services.processBuildStep();

        return new Module() {
            @Override
            public void start() {
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
            }

            @Override
            public void stop() {
                Spark.stop();
            }
        };
    }
}
