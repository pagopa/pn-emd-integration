package it.pagopa.pn.emd.integration.cache;

public enum RedisMode {
    SERVERLESS("serverless"),
    MANAGED("managed");

    private final String value;

    RedisMode(String value) {
        this.value = value;
    }
}
