package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.DominantLanguageService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link DominantLanguageProcessor}
 */
public class DominantLanguageProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        DominantLanguageService mockService = Mockito.mock(DominantLanguageService.class);
        Mockito.when(mockService.getDominantLanguages("foo", 0f)).thenReturn(Collections.singletonList("en"));

        DominantLanguageProcessor processor = new DominantLanguageProcessor("tag", "source",
                "target", 0f, 0, false, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        List<?> values = ingestDocument.getFieldValue("target", List.class);
        assertEquals(1, values.size());
        assertEquals("en", values.get(0));
    }

    public void testExecuteWithSingleMaxValue() throws Exception {
        DominantLanguageService mockService = Mockito.mock(DominantLanguageService.class);
        Mockito.when(mockService.getDominantLanguages("foo", 0f)).thenReturn(Collections.singletonList("en"));

        DominantLanguageProcessor processor = new DominantLanguageProcessor("tag", "source",
                "target", 0f, 1, false, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        String value = ingestDocument.getFieldValue("target", String.class);
        assertEquals("en", value);
    }

    public void testType() throws Exception {

        DominantLanguageProcessor processor = new DominantLanguageProcessor("tag", "source",
                "target", 0f, 0, false, null);
        assertEquals(DominantLanguageProcessor.TYPE, processor.getType());
    }
}