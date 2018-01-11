package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.SentimentService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link SentimentProcessor}
 */
public class SentimentProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        SentimentService mockService = createMockService("FOOBAR");

        SentimentProcessor processor = new SentimentProcessor("tag", "source",
                "target", "en", 0f, 1, false, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);
        String value = ingestDocument.getFieldValue("target", String.class);
        assertEquals("FOOBAR", value);
    }

    public void testType() throws Exception {
        SentimentProcessor processor = new SentimentProcessor("tag", "source",
                "target", "en", 0f, 0, false, null);
        assertEquals(SentimentProcessor.TYPE, processor.getType());
    }

    private SentimentService createMockService(String sentimemt) {
        SentimentService mockService = Mockito.mock(SentimentService.class);
        Mockito.when(mockService.detectSentiment("foo", "en",0f)).thenReturn(sentimemt);
        Mockito.when(mockService.isLanguageCodeValid("en")).thenReturn(true);
        Mockito.when(mockService.isLanguageCodeValid(not(eq("en")))).thenReturn(false);
        return mockService;
    }
}