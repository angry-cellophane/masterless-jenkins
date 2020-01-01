package org.ka.junkins.storage.server.model;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NamingStrategy(convention = NamingConvention.SNAKE_CASE_INSENSITIVE)
public class Build {
    UUID buildId;
    UUID jobId;
    int number;
    String status;
    String result;
    Instant startTs;
    Instant finishTs;
    Instant lastUpdateTs;
}
