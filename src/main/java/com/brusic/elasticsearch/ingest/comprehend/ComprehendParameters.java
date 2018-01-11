package com.brusic.elasticsearch.ingest.comprehend;

enum ComprehendParameters {

    FIELD("field"),
    TARGET("target_field"),
    LANGUAGE_CODE("language_code"),
    MIN_SCORE("min_score"),
    MAX_VALUES("max_values"),
    IGNORE_MISSING("ignore_missing"),
    TYPES("types");

    private final String name;

    ComprehendParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
