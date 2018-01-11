package com.brusic.aws.comprehend.service;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.KeyPhrase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyPhrasesService extends AwsComprehendService {

    KeyPhrasesService(AmazonComprehend comprehendClient) {
        super(comprehendClient);
    }

    public List<String> detectKeyPhrases(String text, String languageCode, float minScore) {
        DetectKeyPhrasesRequest detectKeyPhrasesRequest = new DetectKeyPhrasesRequest()
                .withText(text)
                .withLanguageCode(languageCode);

        DetectKeyPhrasesResult detectKeyPhrasesResult = getComprehendClient().detectKeyPhrases(detectKeyPhrasesRequest);

        Stream<KeyPhrase> entityStream = detectKeyPhrasesResult.getKeyPhrases().stream();

        if (minScore > 0) {
            entityStream = entityStream.filter(keyPhrase -> keyPhrase.getScore() > minScore);
        }

        return entityStream.map(KeyPhrase::getText).collect(Collectors.toList());
    }
}
