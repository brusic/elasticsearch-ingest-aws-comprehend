package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.KeyPhrasesService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link KeyPhrasesProcessor}
 */
public class KeyPhrasesProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        KeyPhrasesService mockService = createMockService();

        KeyPhrasesProcessor processor = new KeyPhrasesProcessor("tag", "source",
                "target", "en", 0f, 0, false, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        List<?> values = ingestDocument.getFieldValue("target", List.class);
        assertEquals(1, values.size());
        assertEquals("FOO", values.get(0));
    }

    public void testExecuteWithSingleMaxValue() throws Exception {
        KeyPhrasesService mockService = createMockService();

        KeyPhrasesProcessor processor = new KeyPhrasesProcessor("tag", "source",
                "target", "en", 0f, 1, false, mockService);

        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        String value = ingestDocument.getFieldValue("target", String.class);
        assertEquals("FOO", value);
    }

    public void testType() throws Exception {
        KeyPhrasesProcessor processor = new KeyPhrasesProcessor("tag", "source",
                "target", "en", 0f, 1, false, null);
        assertEquals(KeyPhrasesProcessor.TYPE, processor.getType());
    }

    private KeyPhrasesService createMockService() {
        KeyPhrasesService mockService = Mockito.mock(KeyPhrasesService.class);
        Mockito.when(mockService.detectKeyPhrases("foo", "en", 0f)).thenReturn(Collections.singletonList("FOO"));
        Mockito.when(mockService.isLanguageCodeValid("en")).thenReturn(true);
        Mockito.when(mockService.isLanguageCodeValid(not(eq("en")))).thenReturn(false);

        return mockService;
    }
}