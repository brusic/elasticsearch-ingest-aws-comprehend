package com.brusic.elasticsearch.ingest.comprehend;

import org.elasticsearch.ingest.IngestDocument;

import java.util.HashMap;
import java.util.Map;

class TestUtils {

    static IngestDocument createTestDocument() {
        Map<String, Object> document = new HashMap<>();
        document.put("source", "foo");
        return createTestDocument(document);
    }

    static IngestDocument createTestDocument(Map<String, Object> document) {
        return new IngestDocument("index", "type", "id", null, null, null,
                null, document);
    }
}
