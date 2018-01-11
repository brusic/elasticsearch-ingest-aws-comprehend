package com.brusic.aws.comprehend.service;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageRequest;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageResult;
import com.amazonaws.services.comprehend.model.DominantLanguage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DominantLanguageService extends AwsComprehendService {

    DominantLanguageService(AmazonComprehend comprehendClient) {
        super(comprehendClient);
    }

    public List<String> getDominantLanguages(String text, float minScore) {
        DetectDominantLanguageRequest detectDominantLanguageRequest = new DetectDominantLanguageRequest()
                .withText(text);

        DetectDominantLanguageResult detectDominantLanguageResult = getComprehendClient().detectDominantLanguage
                (detectDominantLanguageRequest);

        Stream<DominantLanguage> languageStream = detectDominantLanguageResult.getLanguages().stream();
        if (minScore > 0) {
            languageStream = languageStream.filter(lang -> lang.getScore() > minScore);
        }

        return languageStream.map(DominantLanguage::getLanguageCode).collect(Collectors.toList());
    }
}
