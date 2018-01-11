package com.brusic.aws.comprehend.service;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.brusic.aws.comprehend.model.Entity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @see
  <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc//com/amazonaws/services/comprehend/model/EntityType.html">
    EntityType
  </a>
 */
public class EntitiesService extends AwsComprehendService {

    EntitiesService(AmazonComprehend comprehendClient) {
        super(comprehendClient);
    }

    public List<Entity> detectEntities(String text, String languageCode, float minScore,
                                       Collection<String> validTypes) {
        DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest()
                .withText(text)
                .withLanguageCode(languageCode);

        DetectEntitiesResult detectEntitiesResult  = getComprehendClient().detectEntities(detectEntitiesRequest);
        Stream<com.amazonaws.services.comprehend.model.Entity> entityStream = detectEntitiesResult.getEntities().stream();

        if (minScore > 0) {
            entityStream = entityStream.filter(entity -> entity.getScore() > minScore);
        }

        if (validTypes != null && !validTypes.isEmpty()) {
            entityStream = entityStream.filter(entity -> validTypes.contains(entity.getType()));
        }

        return entityStream.map(entity -> new Entity(entity.getText(), entity
                .getType())).collect(Collectors.toList());
    }
}
