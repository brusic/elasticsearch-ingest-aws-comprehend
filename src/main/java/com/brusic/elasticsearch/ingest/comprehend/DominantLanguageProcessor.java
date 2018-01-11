package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.DominantLanguageService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DominantLanguageProcessor extends AbstractAwsProcessor {

    static final String TYPE = "detect-dominant-language";

    private final DominantLanguageService dominantLanguageService;

    DominantLanguageProcessor(String tag,
                              String sourceField,
                              String targetField,
                              Float minScore,
                              Integer maxValues,
                              boolean ignoreMissing,
                              DominantLanguageService dominantLanguageService) {
        // language code is not needed since that is exactly what this processor is meant to detect!
        super(tag, sourceField, targetField, minScore, maxValues, null, ignoreMissing);
        this.dominantLanguageService = dominantLanguageService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) throws Exception {
        Optional<String> text = getTextAndValidate(ingestDocument);
        if (!text.isPresent()) {
            return;
        }

        List<String> dominantLanguages = dominantLanguageService.getDominantLanguages(text.get(), minScore);
        setTargetField(ingestDocument, dominantLanguages);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory {
        private final DominantLanguageService dominantLanguageService;

        Factory(DominantLanguageService dominantLanguageService) {
            this.dominantLanguageService = dominantLanguageService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object>
                config) throws
                Exception {
            readCommonProperties(TYPE, tag, config, "_language");
            return new DominantLanguageProcessor(tag, source, target, minScore, maxValues, ignoreMissing,
                    dominantLanguageService);
        }
    }
}
