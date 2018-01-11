package com.brusic.aws.comprehend.service;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.SentimentScore;
import com.amazonaws.services.comprehend.model.SentimentType;

/**
 * @see
  <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc//com/amazonaws/services/comprehend/model/SentimentType.html">
    SentimentType
  </a>
 */
public class SentimentService extends AwsComprehendService {

    private static final String POSITIVE = SentimentType.POSITIVE.toString();
    private static final String NEGATIVE = SentimentType.NEGATIVE.toString();
    private static final String NEUTRAL = SentimentType.NEUTRAL.toString();
    private static final String MIXED = SentimentType.MIXED.toString();

    SentimentService(
            AmazonComprehend comprehendClient) {
        super(comprehendClient);
    }

    public String detectSentiment(String text, String languageCode, float minScore) {
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest()
                .withText(text)
                .withLanguageCode(languageCode);

        if (!isLanguageCodeValid(languageCode)) {
            return null;
        }

        DetectSentimentResult detectSentimentResult = getComprehendClient().detectSentiment(detectSentimentRequest);

        String overallSentiment = detectSentimentResult.getSentiment();

        if (minScore > 0) {
            SentimentScore sentimentScore = detectSentimentResult.getSentimentScore();

            float overallSentimentScore;
            if (POSITIVE.equals(overallSentiment)) {
                overallSentimentScore = sentimentScore.getPositive();
            } else if (NEGATIVE.equals(overallSentiment)) {
                overallSentimentScore = sentimentScore.getNegative();
            } else if (NEUTRAL.equals(overallSentiment)) {
                overallSentimentScore = sentimentScore.getNeutral();
            } else  {
                // default to MIXED
                overallSentimentScore = sentimentScore.getMixed();
            }

            if (overallSentimentScore < minScore) {
                return null;
            }
        }

        return overallSentiment;
    }
}
