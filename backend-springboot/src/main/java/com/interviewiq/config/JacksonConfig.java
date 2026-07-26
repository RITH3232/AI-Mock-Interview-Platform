package com.interviewiq.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        mapper.setSerializerFactory(mapper.getSerializerFactory().withSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                List<BeanPropertyWriter> newProperties = new ArrayList<>();
                for (BeanPropertyWriter writer : beanProperties) {
                    newProperties.add(writer);
                    if ("id".equals(writer.getName())) {
                        try {
                            BeanPropertyWriter underscoreWriter = writer.rename(new com.fasterxml.jackson.databind.util.NameTransformer() {
                                @Override
                                public String transform(String name) {
                                    return "_id";
                                }
                                @Override
                                public String reverse(String transformed) {
                                    return "id";
                                }
                            });
                            newProperties.add(underscoreWriter);
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
                return newProperties;
            }
        }));

        return mapper;
    }
}
