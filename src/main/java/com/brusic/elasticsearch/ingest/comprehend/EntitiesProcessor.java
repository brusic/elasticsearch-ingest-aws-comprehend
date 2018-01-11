package com.brusic.elasticsearch.ingest.comprehend;

import com.brusic.aws.comprehend.model.Entity;
import com.brusic.aws.comprehend.service.EntitiesService;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalList;

public class EntitiesProcessor extends AbstractAwsProcessor {

    static final String TYPE = "detect-entities";

    private final EntitiesService entitiesService;
    private final List<String> validTypes;

    EntitiesProcessor(String tag,
                      String sourceField,
                      String targetField,
                      String languageCode,
                      Float minScore,
                      Integer maxValues,
                      List<String> validTypes,
                      boolean ignoreMissing,
                      EntitiesService entitiesService) {
        super(tag, sourceField, targetField, minScore, maxValues, languageCode, ignoreMissing);
        this.validTypes = validTypes;
        this.entitiesService = entitiesService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) throws Exception {
        validateLanguage(entitiesService);
        Optional<String> text = getTextAndValidate(ingestDocument);
        if (!text.isPresent()) {
            return;
        }

        List<Entity> entities = entitiesService.detectEntities(text.get(), languageCode, minScore, validTypes);

        Stream<Entity> entityStream = entities.stream();


        if (validTypes != null && validTypes.size() > 0) {
            entityStream = entityStream.filter(entity -> validTypes.contains(entity.getType()));
        }

        List<Map<String, Object>> result = entityStream
                .map(entity -> {
                    HashMap<String, Object> entityMap = new HashMap<>();
                    entityMap.put("text", entity.getText());
                    entityMap.put("type", entity.getType());
                    return entityMap;
                })
                .collect(Collectors.toList());

        setTargetField(ingestDocument, result);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory {
        private final EntitiesService entitiesService;

        Factory(EntitiesService entitiesService) {
            this.entitiesService = entitiesService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) throws
                Exception {
            readCommonProperties(TYPE, tag, config, "_entities");
            List<String> validTypes = readOptionalList(TYPE, tag, config, ComprehendParameters.TYPES.getName());

            return new EntitiesProcessor(tag, source, target, languageCode, minScore, maxValues, validTypes,
                    ignoreMissing, entitiesService);
        }
    }

}
