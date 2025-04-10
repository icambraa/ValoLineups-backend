package com.valolineups.backend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class QueryNormalizer {

    private Map<String, String> aliasMap = new HashMap<>();

    @PostConstruct
    public void loadAliases() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/alias.json");
            aliasMap = mapper.readValue(is, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String normalize(String query) {
        String result = query.toLowerCase();
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            result = result.replace(entry.getKey().toLowerCase(), entry.getValue());
        }
        return result;
    }
}
