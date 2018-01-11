package com.brusic.aws.comprehend.service;

import com.amazonaws.auth.AWSCredentials;

/*
Sigh. One day I will write a proper abstraction/provider for the various services. This simple class
will have to do for now.
 */
public interface ServiceRegistry {

    static ServiceRegistry createAwsRegistry(AWSCredentials credentials, String region) {
        return new AwsServiceRegistry(credentials, region);
    }

    DominantLanguageService getDominantLanguageService();

    EntitiesService getEntitiesService();

    KeyPhrasesService getKeyPhrasesService();

    SentimentService getSentimentService();
}
