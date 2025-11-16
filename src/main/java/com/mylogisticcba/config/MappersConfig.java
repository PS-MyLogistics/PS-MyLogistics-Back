package com.mylogisticcba.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MappersConfig {
    @Primary
    @Bean
    public ModelMapper defaultModelMapper() {
        return new ModelMapper();
    }
    @Bean
    public ModelMapper mergeMaper(){
        ModelMapper maper = new ModelMapper();
        maper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return  maper;
    }
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
