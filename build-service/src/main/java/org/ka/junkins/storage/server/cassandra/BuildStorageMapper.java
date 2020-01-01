package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import org.ka.junkins.storage.server.model.Build;
import org.ka.junkins.storage.server.model.BuildStep;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface BuildStorageMapper {
    @Dao
    interface BuildDao {
        @Query("update junkins.builds set build_id = :buildId, status = :status, result = :result, last_update_ts = toTimestamp(now()), start_ts = :startTs, finish_ts = :finishTs where job_id = :jobId and number = :number")
        void updateBuild(UUID jobId, int number, UUID buildId, String status, String result, Instant startTs, Instant finishTs);

        @Query("select build_id, job_id, number, status, result, start_ts, finish_ts, last_update_ts from junkins.builds where job_id = :jobId and number = :number")
        Optional<Build> findBuild(UUID jobId, int number);

        @Query("select build_id, job_id, number, status, result, start_ts, finish_ts, last_update_ts from junkins.builds where job_id = :jobId")
        PagingIterable<Build> findBuildByJob(UUID jobId);
    }

    @Dao
    interface BuildStepDao {
        @Query("update junkins.build_steps set name = :name, parent_id = :parentId, start_ts = :startTs, last_update_ts = toTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void newBuildStep(UUID buildId, int stepId, int parentId, String name, Instant startTs);

        @Query("update junkins.build_steps set name = :name, parent_id = :parentId, result = :result, log = :log, start_ts = :startTs, finish_ts = :finishTs, last_update_ts = toTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void insertFinishedBuildStep(UUID buildId, int stepId, int parentId, String name, String result, String log, Instant startTs, Instant finishTs);

        @Query("update junkins.build_steps set log = :log, last_update_ts = toTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void updateBuildLog(UUID buildId, int stepId, String log);

        @Query("update junkins.build_steps set result = :result, log = :log, finish_ts = :finishTs, last_update_ts = toTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void finishBuildStep(UUID buildId, int stepId, String result, String log, Instant finishTs);


        @Query("select name, build_id, step_id, parent_id, result, log, start_ts, finish_ts, last_update_ts from junkins.build_steps where build_id = :buildId")
        PagingIterable<BuildStep> allBuildSteps(UUID buildId);

        @Query("select name, build_id, step_id, parent_id, result, log, start_ts, finish_ts, last_update_ts from junkins.build_steps where build_id = :buildId and step_id = :stepId")
        Optional<BuildStep> findBuildStep(UUID buildId, int stepId);
    }

    @DaoFactory
    BuildDao buildDao();
    @DaoFactory
    BuildStepDao buildStepDao();
}
