package com.brusic.aws.comprehend.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import org.elasticsearch.SpecialPermission;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class AwsServiceRegistry implements ServiceRegistry {

    private SentimentService sentimentService;
    private EntitiesService entitiesService;
    private DominantLanguageService languageService;
    private KeyPhrasesService keyPhrasesService;

    AwsServiceRegistry(AWSCredentials credentials, String region) {

        // creating an AmazonComprehend client requires to be run inside
        // a privileged code block
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            // unprivileged code such as scripts do not have SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }

        AmazonComprehend comprehendClient = AccessController.doPrivileged(
                (PrivilegedAction<AmazonComprehend>) () ->
                AmazonComprehendClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withRegion(region)
                        .build()
        );

        sentimentService = new SentimentService(comprehendClient);
        entitiesService = new EntitiesService(comprehendClient);
        languageService = new DominantLanguageService(comprehendClient);
        keyPhrasesService = new KeyPhrasesService(comprehendClient);
    }

    public SentimentService getSentimentService() {
        return sentimentService;
    }

    public EntitiesService getEntitiesService() {
        return entitiesService;
    }

    public DominantLanguageService getDominantLanguageService() {
        return languageService;
    }

    public KeyPhrasesService getKeyPhrasesService() {
        return keyPhrasesService;
    }
}
