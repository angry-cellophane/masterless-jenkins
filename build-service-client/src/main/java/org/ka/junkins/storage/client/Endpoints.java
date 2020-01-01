package org.ka.junkins.storage.client;

import org.ka.junkins.http.Endpoint;

public class Endpoints {

    public static class V1 {
        private static final Endpoint VERSION = Endpoint.create("/v1");

        private static final Endpoint INGEST = Endpoint.create(VERSION, "/ingest");
        private static final Endpoint POST_BUILD_INFO = Endpoint.create(INGEST, "/build");
        private static final Endpoint POST_BUILD_STEP_INFO = Endpoint.create(INGEST, "/build_step");

        public final String POST_BUILD;
        public final String POST_BUILD_STEP;

        private V1(String POST_BUILD, String POST_BUILD_STEP) {
            this.POST_BUILD = POST_BUILD;
            this.POST_BUILD_STEP = POST_BUILD_STEP;
        }

        private static String normalize(String baseUrl) {
            return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        public static V1 from(String baseUrl) {
            var url = normalize(baseUrl);
            return new V1(
                    url + POST_BUILD_INFO.getFullPath(),
                    url + POST_BUILD_STEP_INFO.getFullPath()
            );
        }
    }
}
