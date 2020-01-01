package org.ka.junkins.storage.client;

import org.apache.avro.specific.SpecificRecordBase;

@FunctionalInterface
public interface Client<T extends SpecificRecordBase> {
    void submit(T object);
}
