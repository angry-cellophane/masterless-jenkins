package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.function.Supplier;

public class Mappers {
    public interface Module {
        BuildStorageMapper.BuildStepDao buildStepDao();
        BuildStorageMapper.BuildDao buildDao();
    }

    public static Cassandra.Module create(Supplier<CqlSession> sessionSupplier) {
        var session = sessionSupplier.get();

        var mapper = new BuildStorageMapperBuilder(session).build();
        return new Cassandra.Module() {
            @Override
            public BuildStorageMapper.BuildStepDao buildStepDao() {
                return mapper.buildStepDao();
            }

            @Override
            public BuildStorageMapper.BuildDao buildDao() {
                return mapper.buildDao();
            }
        };
    }
}
