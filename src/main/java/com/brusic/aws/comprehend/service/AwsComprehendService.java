package com.brusic.aws.comprehend.service;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.LanguageCode;
import org.elasticsearch.common.logging.Loggers;

public abstract class AwsComprehendService {

    private final AmazonComprehend comprehendClient;

    AwsComprehendService(AmazonComprehend comprehendClient) {
        this.comprehendClient = comprehendClient;
    }

    AmazonComprehend getComprehendClient() {
        return comprehendClient;
    }

    // only defined LanguageCode enums are valid
    public boolean isLanguageCodeValid(String languageCode) {
        try {
            return LanguageCode.fromValue(languageCode) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
