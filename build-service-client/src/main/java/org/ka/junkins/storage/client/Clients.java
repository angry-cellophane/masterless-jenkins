package org.ka.junkins.storage.client;

import org.apache.avro.specific.SpecificRecordBase;
import org.ka.junkins.avro.Avro;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Clients {

    private static final Logger LOG = Logger.getLogger(Clients.class.getName());

    private final Map<Class<? extends SpecificRecordBase>, Client<? extends SpecificRecordBase>> submitters;

    private Clients(Map<Class<? extends SpecificRecordBase>, Client<? extends SpecificRecordBase>> submitters) {
        this.submitters = submitters;
    }

    @SuppressWarnings("unchecked")
    public <T extends SpecificRecordBase> Client<T> of(Class<T> clazz) {
        return (Client<T>) submitters.get(clazz);
    }

    public static Clients create(String baseUrl) {
        var http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        var V1 = Endpoints.V1.from(baseUrl);
        Map<Class<? extends SpecificRecordBase>, Client<? extends SpecificRecordBase>> submitters = Map.of(
                Build.class, newSubmitter(http, V1.POST_BUILD),
                BuildStep.class, newSubmitter(http, V1.POST_BUILD_STEP)
        );
        return new Clients(submitters);
    }

    private static <T extends SpecificRecordBase> Client<T> newSubmitter(HttpClient http, String endpoint) {
        return (object) -> {
            try {
                var request = HttpRequest.newBuilder()
                        .uri(new URI(endpoint))
                        .POST(HttpRequest.BodyPublishers.ofInputStream(() -> Avro.serialize(object)))
                        .build();

                var response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) {
                    LOG.log(Level.SEVERE, "unexpected response " + response.statusCode() + " " + response.body());
                }
            } catch (Exception e) {
                throw new ClientException(e);
            }
        };
    }
}
