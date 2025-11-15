package com.mylogisticcba.iam.tenant.services.impl;

import com.mylogisticcba.iam.tenant.dtos.TenantConfigDto;
import com.mylogisticcba.iam.tenant.entity.TenantConfigEntity;
import com.mylogisticcba.iam.repositories.TenantConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
@AllArgsConstructor
@Service
public class TenantConfigService {

    private final TenantConfigRepository repository;


    public Map<String, String> getConfigForTenant(UUID tenantId) {
        List<TenantConfigEntity> configs = repository.findByTenantId(tenantId);

        return configs.stream()
                .collect(Collectors.toMap(
                        TenantConfigEntity::getKey,
                        TenantConfigEntity::getValue
                ));
    }
    public Integer getIntValue(UUID tenantId, String key, Integer defaultValue) {
        return repository.findByTenantId(tenantId).stream()
                .filter(cfg -> cfg.getKey().equals(key))
                .map(TenantConfigEntity::getValue)
                .findFirst()
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }
    public Boolean getBoolValue(UUID tenantId, String key, Boolean defaultValue) {
        return repository.findByTenantId(tenantId).stream()
                .filter(cfg -> cfg.getKey().equals(key))
                .map(TenantConfigEntity::getValue)
                .findFirst()
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }
    public String getStringValue(UUID tenantId, String key, String defaultValue) {
        return repository.findByTenantId(tenantId).stream()
                .filter(cfg -> cfg.getKey().equals(key))
                .map(TenantConfigEntity::getValue)
                .findFirst()
                .orElse(defaultValue);
    }

    public TenantConfigDto getParsedConfig(UUID tenantId) {
        List<TenantConfigEntity> configs = repository.findByTenantId(tenantId);

        TenantConfigDto dto = new TenantConfigDto();

        for (TenantConfigEntity config : configs) {
            String value = config.getValue();
            Object parsedValue = parseValue(value);
            dto.addConfig(config.getKey(), parsedValue);
        }

        return dto;
    }

    private Object parseValue(String value) {
        // if value is null or empty, return null
        if (value == null ) {
            return "";
        }
        // try to parse to Integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        // try to parse to Boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        // default to String

        return value;
    }

    public void saveConfig(UUID tenantId, String key, String value) {
        TenantConfigEntity entity = repository.findByTenantId(tenantId).stream()
                .filter(cfg -> cfg.getKey().equals(key))
                .findFirst()
                .orElse(new TenantConfigEntity(UUID.randomUUID(),tenantId, key, value));

        entity.setValue(value);
        repository.save(entity);
    }

    public void deleteConfig(UUID tenantId, String key) {
        List<TenantConfigEntity> configs = repository.findByTenantId(tenantId);
        configs.stream()
                .filter(cfg -> cfg.getKey().equals(key))
                .findFirst()
                .ifPresent(repository::delete);
    }

    public void updateConfig(UUID tenantId, String key, String value) {
        try {
            List<TenantConfigEntity> configs = repository.findByTenantId(tenantId);
            configs.stream()
                    .filter(cfg -> cfg.getKey().equals(key))
                    .findFirst()
                    .ifPresent(cfg -> {
                        cfg.setValue(value);
                        repository.save(cfg);
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}
