package org.ka.junkins.storage.server.cassandra;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import org.ka.junkins.storage.server.model.Build;
import org.ka.junkins.storage.server.model.BuildStep;

import java.util.Optional;

@Mapper
public interface BuildStorageMapper {
    @Dao
    interface BuildDao {
        @Query("update junkins.builds set build_id = :buildId, status = :status, result = :result, last_update_ts = toUnixTimestamp(now()), started_ts = :startedTs, finished_ts = :finishedTs where job_id = :jobId and number = :number")
        void updateBuild(String jobId, int number, String buildId, String status, String result, Long startedTs, Long finishedTs);

        @Query("select build_id, job_id, number, status, result, started_ts, finished_ts, last_update_ts from junkins.builds where job_id = :jobId and number = :number")
        Optional<Build> findBuild(String jobId, int number);

        @Query("select build_id, job_id, number, status, result, started_ts, finished_ts, last_update_ts from junkins.builds where job_id = :jobId")
        PagingIterable<Build> findBuildByJob(String jobId);
    }

    @Dao
    interface BuildStepDao {
        @Query("update junkins.build_steps set name = :name, parent_id = :parentId, started_ts = :startTs, last_update_ts = toUnixTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void newBuildStep(String buildId, int stepId, int parentId, String name, Long startTs);

        @Query("update junkins.build_steps set name = :name, parent_id = :parentId, result = :result, log = :log, started_ts = :startedTs, finished_ts = :finishedTs, last_update_ts = toUnixTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void insertFinishedBuildStep(String buildId, int stepId, int parentId, String name, String result, String log, Long startedTs, Long finishedTs);

        @Query("update junkins.build_steps set log = :log, last_update_ts = toUnixTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void updateBuildLog(String buildId, int stepId, String log);

        @Query("update junkins.build_steps set result = :result, log = :log, finished_ts = :finishedTs, last_update_ts = toUnixTimestamp(now()) where build_id = :buildId and step_id = :stepId")
        void finishBuildStep(String buildId, int stepId, String result, String log, Long finishedTs);


        @Query("select name, build_id, step_id, parent_id, result, log, started_ts, finished_ts, last_update_ts from junkins.build_steps where build_id = :buildId")
        PagingIterable<BuildStep> allBuildSteps(String buildId);

        @Query("select name, build_id, step_id, parent_id, result, log, started_ts, finished_ts, last_update_ts from junkins.build_steps where build_id = :buildId and step_id = :stepId")
        Optional<BuildStep> findBuildStep(String buildId, int stepId);
    }

    @DaoFactory
    BuildDao buildDao();

    @DaoFactory
    BuildStepDao buildStepDao();
}
