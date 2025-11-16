package com.mylogisticcba.iam.tenant.dtos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TenantConfigDto {
    private Map<String, Object> configs = new HashMap<>();

    @JsonAnySetter
    public void addConfig(String key, Object value) {
        configs.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getConfigs() {
        return configs;
    }

    public Object get(String key) {
        return configs.get(key);
    }
}
