package com.brusic.elasticsearch.ingest.comprehend;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests for {@link AbstractAwsProcessor}
 */
public class AbstractAwsProcessorTests extends LuceneTestCase {

    public void testNoMaxValues() throws Exception {
        final List<String> values = Arrays.asList("foo", "bar", "baz");
        AbstractAwsProcessor awsProcessor = createProcessor(0f, 0, values, false);
        IngestDocument ingestDocument = TestUtils.createTestDocument();

        awsProcessor.execute(ingestDocument);
        List<?> value = ingestDocument.getFieldValue("target", List.class);
        assertEquals(values.size(), value.size());
    }

    public void testMaxValues() throws Exception {
        final List<String> values = Arrays.asList("foo", "bar", "baz");
        AbstractAwsProcessor awsProcessor = createProcessor(0f, 2, values, false);
        IngestDocument ingestDocument = TestUtils.createTestDocument();

        awsProcessor.execute(ingestDocument);
        List<?> value = ingestDocument.getFieldValue("target", List.class);
        assertEquals(2, value.size());
    }

    public void testSingleMaxValues() throws Exception {
        final List<String> values = Arrays.asList("foo", "bar", "baz");
        AbstractAwsProcessor awsProcessor = createProcessor(0f, 1, values, false);
        IngestDocument ingestDocument = TestUtils.createTestDocument();

        awsProcessor.execute(ingestDocument);
        String value = ingestDocument.getFieldValue("target", String.class);
        assertEquals("foo", value);
    }

    public void testIgnoreMissing()  {
        final List<String> values = Arrays.asList("foo", "bar", "baz");
        AbstractAwsProcessor awsProcessor = createProcessor(0f, 1, values, true);

        Map<String, Object> document = new HashMap<>();
        document.put("foo", "bar");
        IngestDocument ingestDocument = TestUtils.createTestDocument(document);

        try {
            awsProcessor.execute(ingestDocument);
            ingestDocument.getFieldValue("target", String.class);
            fail("exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
            assertEquals(e.getMessage(), "field [target] not present as part of path [target]");
        }
    }

    public void testIgnoreMissingDefaultFalse()  {
        final List<String> values = Arrays.asList("foo", "bar", "baz");
        AbstractAwsProcessor awsProcessor = createProcessor(0f, 1, values, false);

        Map<String, Object> document = new HashMap<>();
        document.put("foo", "bar");
        IngestDocument ingestDocument = TestUtils.createTestDocument(document);

        try {
            awsProcessor.execute(ingestDocument);
            fail("exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
            assertEquals(e.getMessage(), "field [source] is null, cannot parse text.");
        }
    }

    public void testReadFloatProperty() {
        Map<String, Object> config = new HashMap<>();
        config.put("foo", 13.37f);
        Float foo = AbstractAwsProcessor.readFloatProperty(null, null, config, "foo", 0f);
        assertThat(foo, equalTo(13.37f));
    }

    public void testReadDefaultFloatProperty() {
        Map<String, Object> config = new HashMap<>();
        Float foo = AbstractAwsProcessor.readFloatProperty(null, null, config, "foo", 10f);
        assertThat(foo, equalTo(10f));
    }

    private AbstractAwsProcessor createProcessor(float minScore, int maxValues, List<String> values,
                                                 boolean missingValues) {
        return new AbstractAwsProcessor("tag", "source", "target", minScore, maxValues,
                "en", missingValues) {
            @Override
            public void execute(IngestDocument ingestDocument) throws Exception {
                Optional<String> text = getTextAndValidate(ingestDocument);
                if (!text.isPresent()) {
                    return;
                }

                setTargetField(ingestDocument, values);
            }

            @Override
            public String getType() {
                return "foo";
            }
        };
    }
}