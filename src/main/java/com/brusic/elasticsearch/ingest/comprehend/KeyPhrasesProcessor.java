package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.service.KeyPhrasesService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KeyPhrasesProcessor extends AbstractAwsProcessor {

    static final String TYPE = "detect-key-phrases";

    private final KeyPhrasesService keyPhrasesService;

    KeyPhrasesProcessor(String tag,
                        String sourceField,
                        String targetField,
                        String languageCode,
                        Float minScore,
                        Integer maxValues,
                        boolean ignoreMissing,
                        KeyPhrasesService keyPhrasesService) {
        super(tag, sourceField, targetField, minScore, maxValues, languageCode, ignoreMissing);
        this.keyPhrasesService = keyPhrasesService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) throws Exception {
        validateLanguage(keyPhrasesService);
        Optional<String> text = getTextAndValidate(ingestDocument);
        if (!text.isPresent()) {
            return;
        }

        List<String> phrases = keyPhrasesService.detectKeyPhrases(text.get(), languageCode, minScore);
        setTargetField(ingestDocument, phrases);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory {
        private final KeyPhrasesService keyPhrasesService;

        Factory(KeyPhrasesService keyPhrasesService) {
            this.keyPhrasesService = keyPhrasesService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) throws
                Exception {
            readCommonProperties(TYPE, tag, config, "_keyphrases");
            return new KeyPhrasesProcessor(tag, source, target, languageCode, minScore, maxValues,
                    ignoreMissing, keyPhrasesService);
        }
    }
}
