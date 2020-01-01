package org.ka.junkins.storage.server.cassandra;

public class Mapping {
    public interface Module {
        BuildStorageMapper.BuildStepDao buildStepDao();
        BuildStorageMapper.BuildDao buildDao();
    }

    public static Mapping.Module create(Cassandra.Module cassandra) {
        var supplier = cassandra.sessionSupplier();
        var session = supplier.get();

        var mapper = new BuildStorageMapperBuilder(session)
                .withDefaultKeyspace("junkins")
                .build();
        return new Module() {
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
