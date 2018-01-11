package com.brusic.elasticsearch.ingest.comprehend;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.brusic.aws.comprehend.service.DominantLanguageService;
import com.brusic.aws.comprehend.service.EntitiesService;
import com.brusic.aws.comprehend.service.KeyPhrasesService;
import com.brusic.aws.comprehend.service.SentimentService;
import com.brusic.aws.comprehend.service.ServiceRegistry;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.SecureSetting;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IngestAwsComprehendPlugin extends Plugin implements IngestPlugin {

    private static String DEFAULT_REGION = Regions.US_EAST_1.getName();

    private static final Setting<SecureString> ACCESS_KEY_SETTING =
            SecureSetting.secureString("ingest.aws-comprehend.credentials.access_key", null);

    private static final Setting<SecureString> SECRET_KEY_SETTING =
            SecureSetting.secureString("ingest.aws-comprehend.credentials.secret_key", null);

    private static final Setting<String> REGION_SETTING = new Setting<>("ingest.aws-comprehend.region",
            DEFAULT_REGION, Function.identity(), Setting.Property.NodeScope);

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        Map<String, Processor.Factory> processors = new HashMap<>();

        Settings settings = parameters.env.settings();
        Logger logger = Loggers.getLogger(getClass(), settings);

        SecureString accessKey = ACCESS_KEY_SETTING.get(settings);
        SecureString secretKey = SECRET_KEY_SETTING.get(settings);
        String region = REGION_SETTING.get(settings);

        if  (Strings.isNullOrEmpty(accessKey.toString()) || Strings.isNullOrEmpty(secretKey
                .toString())) {
            logger.warn("Cannot create AWS Comprehend processors since either {} or {} is not defined",
                    ACCESS_KEY_SETTING.getKey(), SECRET_KEY_SETTING.getKey());
        } else {
            AWSCredentials awsCreds = new BasicAWSCredentials(accessKey.toString(), secretKey.toString());
            ServiceRegistry serviceRegistry = ServiceRegistry.createAwsRegistry(awsCreds, region);

            DominantLanguageService dominantLanguageService = serviceRegistry.getDominantLanguageService();
            processors.put(DominantLanguageProcessor.TYPE, new DominantLanguageProcessor.Factory(dominantLanguageService));

            EntitiesService entitiesService = serviceRegistry.getEntitiesService();
            processors.put(EntitiesProcessor.TYPE, new EntitiesProcessor.Factory(entitiesService));

            KeyPhrasesService keyPhrasesService = serviceRegistry.getKeyPhrasesService();
            processors.put(KeyPhrasesProcessor.TYPE, new KeyPhrasesProcessor.Factory(keyPhrasesService));

            SentimentService sentimentService = serviceRegistry.getSentimentService();
            processors.put(SentimentProcessor.TYPE, new SentimentProcessor.Factory(sentimentService));
        }

        return processors;
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
                ACCESS_KEY_SETTING,
                SECRET_KEY_SETTING,
                REGION_SETTING
        );
    }
}
