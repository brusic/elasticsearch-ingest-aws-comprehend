# Elasticsearch Amazon Comprehend NLP Ingest Processor

[Elasticsearch ingest processors](https://www.elastic.co/guide/en/elasticsearch/reference/master/ingest-processors.html) using [Amazon Comprehend](https://aws.amazon.com/comprehend/) for various NLP analysis. All Comprehend detection features are supported via separate processors. Topic Modeling is not supported, although it would be an interesting project to hook up Elasticsearch as a data source for AWS Comprehend topic modeling.

Each field that is sent through the ingest process will result in an AWS Comprehend API call, so this system is not meant for clusters with large workloads. There is no support for batch processing. For better performance, your Elasticsearch ingest nodes should not only be hosted in AWS, but should also be in the region used in the AWS Comprehend API (configurable). 

## [AWS Comprehend Pricing](https://aws.amazon.com/comprehend/pricing/)
$0.0001 PER UNIT
Up to 10M units

$0.00005 PER UNIT
From 10M - 50M units

$0.000025 PER UNIT
Over 50M units

NLP requests are measured in units of 100 characters, with a 3 unit (300 character) minimum charge per request.

## Supported Features
* [Keyphrase Extraction](https://docs.aws.amazon.com/comprehend/latest/dg/how-key-phrases.html)
* [Sentiment Analysis](https://docs.aws.amazon.com/comprehend/latest/dg/how-sentiment.html)
* [Entity Recognition](https://docs.aws.amazon.com/comprehend/latest/dg/how-entities.html)
* [Language Detection](https://docs.aws.amazon.com/comprehend/latest/dg/how-languages.html)


## Building

There is no downloadable version of the plugin for two reasons:

1. It is difficult to release a plugin for each minor version of Elasticsearch. You can only run plugins built for the exact version of Elasticsearch.
2. Due to the warning at the very top regarding cost and performance, it prefered that the plugin is built and not blindly installed so that users are aware. 

Only Elasticsearch 5.6+ is supported in order to take advantage of the secure keystore.

## Installation

Only basic credentials are supported. The AWS access and secret keys are added to [Elasticsearch keystore](https://www.elastic.co/guide/en/elasticsearch/reference/current/secure-settings.html), before the node is started.


## Plugin Settings
| Setting | Description |
| ---- | ---- |
| ingest.aws-comprehend.credentials.access_key | AWS Acesss key |
| ingest.aws-comprehend.credentials.secret_key | AWS Secret Key |
| ingest.aws-comprehend.region | AWS region used to the API call. Default region is us-east-1 |

AWS Credentials are not configured in elasticsearch.yml, or in the plugin settings, but in the keystore. Settings must be in place before Elasticsearch is started.


## Processor settings

| Name | Required | Default | Description |
| ---- | ---- | ---- | ---- |
| field | yes | - | The field to analyze |
| target_field | no | A new field with the name of the source field with a processor specific suffix appended | The field to assign the converted value to. |
| language_code | no | en | The language of the analyzed text. Not used in the Language Detection processor. |
| min_score | no | 0 (all returned) | The minimum score threshold of values to be returned |
| max_values | no | 0 (all returned) | The number of values to return.  If max_value is 1, a single value is returned and not an array. Not used in the Sentiment Analysis processor. |
| ignore_missing | no | false | If true and field does not exist or is null, the processor quietly exits without modifying the document |
| types | no | empty (all returned) | Filter the values returned by the [entity types](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc//com/amazonaws/services/comprehend/model/EntityType.html) |


| Feature | Processor Name | Default suffix |
| ---- | ---- | --- |
| Keyphrase Extraction | detect-key-phrases | _keyphrases |
| Sentiment Analysis | detect-sentiment | _sentiment |
| Entity Recognition | detect-entities | _entities |
| Language Detection | detect-dominant-language | _language |

## Examples

After each pipeline is configured, the same document is indexed
```
PUT /my-index/my-type/1?pipeline=aws-comprehend-pipeline
{
  "my_field" : "It is raining today in Seattle. Good thing I live in California."
}
```

Language detection
```
PUT _ingest/pipeline/aws-comprehend-pipeline
{
   "description": "A pipeline to test AWS Comprehend",
   "processors": [
      {
         "detect-dominant-language": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field": "It is raining today in Seattle. Good thing I live in California.",
      "my_field_language": [
         "en"
      ]
   }
}
```

Entity detection
```
PUT _ingest/pipeline/aws-comprehend-pipeline
{
   "description": "A pipeline to test AWS Comprehend",
   "processors": [
      {
         "detect-entities": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_entities": [
         {
            "text": "today",
            "type": "DATE"
         },
         {
            "text": "Seattle",
            "type": "LOCATION"
         },
         {
            "text": "California",
            "type": "LOCATION"
         }
      ],
      "my_field": "It is raining today in Seattle. Good thing I live in California."
   }
}
```

Keyphrase Extraction
```
PUT _ingest/pipeline/aws-comprehend-pipeline
{
   "description": "A pipeline to test AWS Comprehend",
   "processors": [
      {
         "detect-key-phrases": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field": "It is raining today in Seattle. Good thing I live in California.",
      "my_field_keyphrases": [
         "today",
         "Seattle",
         "Good thing",
         "California"
      ]
   }
}
```

Sentiment Analysis
```
PUT _ingest/pipeline/aws-comprehend-pipeline
{
   "description": "A pipeline to test AWS Comprehend",
   "processors": [
      {
         "detect-sentiment": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field": "It is raining today in Seattle. Good thing I live in California.",
      "my_field_sentiment": "POSITIVE"
   }
}
```

Change the target field to run different processors on the same field.

```
PUT _ingest/pipeline/aws-comprehend-pipeline
{
   "description": "A pipeline to test AWS Comprehend",
   "processors": [
      {
         "detect-key-phrases": {
            "field": "my_field"
         }
      },
      {
         "detect-key-phrases": {
            "field": "my_field",
            "target_field": "my_field_strict",
            "min_score": 0.9
         }
      },
      {
         "detect-key-phrases": {
            "field": "my_field",
            "target_field": "my_field_trimmed",
            "max_values": 1
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_keyphrases": [
         "today",
         "Seattle",
         "Good thing",
         "California"
      ],
      "my_field_trimmed": "today",
      "my_field_strict": [
         "today",
         "Seattle",
         "California"
      ],
      "my_field": "It is raining today in Seattle. Good thing I live in California."
   }
}
```
