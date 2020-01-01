package org.ka.junkins.avro;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.util.ByteBufferInputStream;
import org.apache.avro.util.ByteBufferOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Avro {

    private static final Map<Class<?>, SpecificDatumReader<?>> READERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, SpecificDatumWriter<?>> WRITERS = new ConcurrentHashMap<>();

    public static <T> T from(Class<T> clazz, byte[] bytes) {
        var reader = READERS.computeIfAbsent(clazz, SpecificDatumReader::new);

        var decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        try {
            return clazz.cast(reader.read(null, decoder));
        } catch (IOException e) {
            throw new JunkinsException(e);
        }
    }

    public static <T> T from(Class<T> clazz, InputStream in) {
        var reader = READERS.computeIfAbsent(clazz, SpecificDatumReader::new);

        var decoder = DecoderFactory.get().binaryDecoder(in, null);
        try {
            return clazz.cast(reader.read(null, decoder));
        } catch (IOException e) {
            throw new JunkinsException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> InputStream serialize(T object) {
        var writer = (SpecificDatumWriter<T>) WRITERS.computeIfAbsent(object.getClass(), SpecificDatumWriter::new);

        try {
            try (var out = new ByteBufferOutputStream()) {
                var encoder = EncoderFactory.get().binaryEncoder(out, null);
                writer.write(object, encoder);
                encoder.flush();

                return new ByteBufferInputStream(out.getBufferList());
            }
        } catch (IOException e) {
            throw new JunkinsException(e);
        }
    }

    private Avro() {}
}
