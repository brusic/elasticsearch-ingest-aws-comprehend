package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.AwsComprehendService;
import com.brusic.aws.comprehend.service.SentimentService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.Map;
import java.util.Optional;

public class SentimentProcessor extends AbstractAwsProcessor {

    static final String TYPE = "detect-sentiment";

    private final SentimentService sentimentService;

    SentimentProcessor(String tag,
                       String sourceField,
                       String targetField,
                       String languageCode,
                       Float minScore,
                       Integer maxValues,
                       boolean ignoreMissing,
                       SentimentService sentimentService) {
        super(tag, sourceField, targetField, minScore, maxValues, languageCode, ignoreMissing);
        this.sentimentService = sentimentService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) throws Exception {
        validateLanguage(sentimentService);
        Optional<String> text = getTextAndValidate(ingestDocument);
        if (!text.isPresent()) {
            return;
        }

        String sentiment = sentimentService.detectSentiment(text.get(), languageCode, minScore);
        setTargetField(ingestDocument, sentiment);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory {
        private final SentimentService sentimentService;

        Factory(SentimentService sentimentService) {
            this.sentimentService = sentimentService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) throws
                Exception {
            readCommonProperties(TYPE, tag, config, "_sentiment");
            return new SentimentProcessor(tag, source, target, languageCode, minScore,
                    maxValues, ignoreMissing, sentimentService);
        }
    }
}
