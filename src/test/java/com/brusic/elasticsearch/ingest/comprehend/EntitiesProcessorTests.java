package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.model.Entity;
import com.brusic.aws.comprehend.service.EntitiesService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link EntitiesProcessor}
 */
public class EntitiesProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        EntitiesService mockService = createMockService();

        EntitiesProcessor processor = new EntitiesProcessor("tag", "source",
                "target", "en", 0f, 0, null,false, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        List<?> values = ingestDocument.getFieldValue("target", List.class);
        assertEquals(1, values.size());
        assertEquals("foo", ((Map)values.get(0)).get("text"));
    }

    public void testExecuteWithSingleMaxValue() throws Exception {
        EntitiesService mockService = createMockService();

        EntitiesProcessor processor = new EntitiesProcessor("tag", "source",
                "target", "en", 0f, 1, null,false, mockService);

        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        Map<?, ?> value = ingestDocument.getFieldValue("target", Map.class);
        assertEquals("BAR", value.get("type"));
    }

    public void testType() throws Exception {
        EntitiesProcessor processor = new EntitiesProcessor("tag", "source",
                "target", "en", 0f, 1, null,false, null);
        assertEquals(EntitiesProcessor.TYPE, processor.getType());
    }

    private EntitiesService createMockService() {
        EntitiesService mockService = Mockito.mock(EntitiesService.class);
        Mockito.when(mockService.detectEntities("foo", "en", 0f, null)).thenReturn(Collections.singletonList(new Entity
                ("foo", "BAR")));
        Mockito.when(mockService.isLanguageCodeValid("en")).thenReturn(true);
        Mockito.when(mockService.isLanguageCodeValid(not(eq("en")))).thenReturn(false);

        return mockService;
    }
}