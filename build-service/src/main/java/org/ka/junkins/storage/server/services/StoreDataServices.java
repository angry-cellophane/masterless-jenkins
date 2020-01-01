package org.ka.junkins.storage.server.services;

import org.ka.junkins.storage.client.Build;
import org.ka.junkins.storage.client.BuildStep;
import org.ka.junkins.storage.server.cassandra.Mapping;

import java.util.Optional;
import java.util.function.Consumer;

public class StoreDataServices {
    public interface Module {
        Consumer<Build> processBuild();
        Consumer<BuildStep> processBuildStep();
    }

    public static Module create(Mapping.Module mappers) {
        var buildDao = mappers.buildDao();
        var buildStepDao = mappers.buildStepDao();

        Consumer<Build> processBuild = build -> {
//            buildDao.updateBuild(build.getJobId(),
//                    build.getNumber(),
//                    build.getBuildId(),
//                    build.getStatus().name(),
//                    Optional.ofNullable(build.getResult()).map(Enum::name).orElse(null)
//            );
        };
        Consumer<BuildStep> processBuildStep = buildStep -> { };
        return new Module() {
           @Override
           public Consumer<Build> processBuild() {
               return processBuild;
           }

           @Override
           public Consumer<BuildStep> processBuildStep() {
               return processBuildStep;
           }
       };
    }
}
