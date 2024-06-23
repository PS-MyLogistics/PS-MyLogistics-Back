package gruop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

public class MappersConfig {

    @Bean
    public ModelMapper  modelMapeper(){
        ModelMapper modelMapper=new ModelMapper();
      return modelMapper;}

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
