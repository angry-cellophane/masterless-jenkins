package org.ka.junkins.storage.client;

import org.apache.avro.specific.SpecificRecordBase;

import java.net.URI;
import java.net.URISyntaxException;

public class Endpoints {
    public static class Endpoint<T extends SpecificRecordBase> {
        final Class<T> type;
        final URI uri;

        public Endpoint(Class<T> type, URI uri) {
            this.type = type;
            this.uri = uri;
        }
    }

    public static class V1 {
        public static final String POST_BUILD_INFO = "/v1/ingest/build";
        public static final String POST_BUILD_STEP_INFO = "/v1/ingest/build_step";

        private final Endpoint<Build> buildInfo;
        private final Endpoint<BuildStep> buildStepInfo;

        private V1(String baseUrl) {
            try {
                this.buildInfo = new Endpoint<>(
                        Build.class,
                        new URI(normalize(baseUrl) + POST_BUILD_INFO)
                );
                this.buildStepInfo = new Endpoint<>(
                        BuildStep.class,
                        new URI(normalize(baseUrl) + POST_BUILD_STEP_INFO)
                );
            } catch (URISyntaxException e) {
                throw new ClientException(e);
            }
        }

        private String normalize(String baseUrl) {
            return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        public Endpoint<Build> postBuildInfo() {
            return buildInfo;
        }

        public Endpoint<BuildStep> postBuildStep() {
            return buildStepInfo;
        }

        public static V1 from(String baseUrl) {
            return new V1(baseUrl);
        }
    }
}
