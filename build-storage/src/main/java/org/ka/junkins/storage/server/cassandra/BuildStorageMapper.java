package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.driver.api.mapper.annotations.Query;

@Mapper
public interface BuildStorageMapper {
    @Dao
    interface BuildDao {
        @Query("update builds")
        void updateBuildInfo();
    }

    @Dao
    interface BuildStepDao {
        @Query("update build_steps")
        void updateBuildSteps();
    }

    @DaoFactory
    BuildDao buildDao();
    @DaoFactory
    BuildStepDao buildStepDao();
}
