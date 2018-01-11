package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.AwsComprehendService;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.ingest.ConfigurationUtils.readIntProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

/**
 * Base class for all AWS Comprehend processors
 *
 * Contains common fields and methods shared by all processors
 */
abstract class AbstractAwsProcessor extends AbstractProcessor {

    static final String DEFAULT_LANG = "en";
    static final int ALL_VALUES = 0;
    static final float DEFAULT_MIN_SCORE = 0f;

    // common parameters
    private final String sourceField;
    private final String targetField;
    private final Integer maxValues;
    private final boolean ignoreMissing;

    final String languageCode; // not used by the dominant language processor
    final Float minScore;

    AbstractAwsProcessor(String tag, String sourceField, String targetField, Float minScore,
                         Integer maxValues, String languageCode, boolean ignoreMissing) {
        super(tag);
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.minScore = minScore;
        this.maxValues = maxValues;
        this.languageCode = languageCode;
        this.ignoreMissing = ignoreMissing;
    }

    /**
     * retrieve the value of the source field from the document
     * if the field is not found or null:
     *     return null if ignoreMissing is true
     *     throw Exception is ignoreMissing is false
     *
     * @param ingestDocument the document source contains the field
     * @return the source field text
     */
    Optional<String> getTextAndValidate(IngestDocument ingestDocument) {
        String text = ingestDocument.getFieldValue(sourceField, String.class, true);

        if (text == null && ignoreMissing) {
            return Optional.empty();
        } else if (text == null) {
            throw new IllegalArgumentException("field [" + sourceField + "] is null, cannot parse text.");
        }

        return Optional.of(text);
    }

    void validateLanguage(AwsComprehendService service) throws IllegalArgumentException {
        boolean languageCodeValid = service.isLanguageCodeValid(languageCode);
        if (!languageCodeValid) {
            throw new IllegalArgumentException("languageCode [" + languageCode + "] is not valid. Please see documentation.");
        }
    }

    void setTargetField(IngestDocument ingestDocument, String value) {
        ingestDocument.setFieldValue(targetField, value);
    }

    void setTargetField(IngestDocument ingestDocument, List<?> list) {
        list = trim(list);
        if (maxValues == 1 && list.size() > 0) {
            outputOne(ingestDocument, targetField, list);
        } else {
            outputAll(ingestDocument, targetField, list);
        }
    }

    /**
     * Package private for tests
     *
     * Returns and removes the specified property from the specified configuration map.
     *
     * If the property value isn't of type float a {@link ElasticsearchParseException} is thrown.
     * If the property is missing an {@link ElasticsearchParseException} is thrown
     */
    static Float readFloatProperty(String processorType, String processorTag, Map<String, Object>
            configuration, String propertyName, Float defaultValue) {
        Object value = configuration.remove(propertyName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (Exception e) {
            throw ConfigurationUtils.newConfigurationException(processorType, processorTag, propertyName,
                    "property cannot be converted to an int [" + value.toString() + "]");
        }
    }

    private <T> List<T> trim(List<T> list) {
        if (maxValues != ALL_VALUES && list.size() > maxValues) {
            list = list.subList(0, maxValues);
        }
        return list;
    }

    private void outputAll(IngestDocument ingestDocument, String targetField, List<?> list) {
        ingestDocument.setFieldValue(targetField, list);
    }

    private void outputOne(IngestDocument ingestDocument, String targetField, List<?> list) {
        ingestDocument.setFieldValue(targetField, list.get(0));
    }

    abstract static class CommonFactory implements Processor.Factory {

        String source;
        String target;
        Float minScore;
        Integer maxValues;
        String languageCode;
        boolean ignoreMissing;

        void readCommonProperties(String type, String tag, Map<String, Object> config, String targetSuffix) {
            source = readStringProperty(type, tag, config, ComprehendParameters.FIELD.getName());
            target = readStringProperty(type, tag, config, ComprehendParameters.TARGET.getName(), source + targetSuffix);
            maxValues = readIntProperty(type, tag, config, ComprehendParameters.MAX_VALUES.getName(), ALL_VALUES);
            minScore = readFloatProperty(type, tag, config, ComprehendParameters.MIN_SCORE.getName(), DEFAULT_MIN_SCORE);
            languageCode = readStringProperty(type, tag, config, ComprehendParameters.LANGUAGE_CODE.getName(), DEFAULT_LANG);
            ignoreMissing = ConfigurationUtils.readBooleanProperty(type, tag, config,
                    ComprehendParameters.IGNORE_MISSING.getName(), false);

        }
    }
}
