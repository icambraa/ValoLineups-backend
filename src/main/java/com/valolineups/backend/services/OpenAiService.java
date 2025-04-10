package com.valolineups.backend.services;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.valolineups.backend.models.SearchFilterResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public SearchFilterResult extractFilters(String userQuery) {
        String prompt = """
                    Eres un experto en Valorant y tu tarea es entender frases y convertirlas en estos parámetros:

                    - "map": el nombre del mapa (por ejemplo, "Ascent")
                    - "agent": el nombre del agente (por ejemplo, "KAY/O")
                    - "side": el lado en el que se juega (por ejemplo, "Attack")
                    - "executedOn": la zona desde la que se lanza la habilidad (por ejemplo, "A Site")
                    - "affectedArea": la zona a la que se lanza la habilidad (por ejemplo, "B Main")

                    Dado el texto:

                    "%s"

                    Devuelve un JSON con los campos anteriores. Si se menciona algo como "A Site hacia B Main" (por ejemplo), separa "A Site" como `executedOn` y "B Main" como `affectedArea`.

                    El formato del JSON que debe devolver es:
                    {
                      "map": "",
                      "agent": "",
                      "side": "",
                      "executedOn": "",
                      "affectedArea": ""
                    }
                """
                .formatted(userQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-3.5-turbo");
        ArrayNode messages = mapper.createArrayNode();
        messages.add(message);
        body.set("messages", messages);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        try {
            JsonNode json = mapper.readTree(response.getBody());
            String raw = json.at("/choices/0/message/content").asText();
            return mapper.readValue(raw, SearchFilterResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Double> generateEmbedding(String input) {
        String embeddingEndpoint = "https://api.openai.com/v1/embeddings";

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "text-embedding-ada-002");
        body.put("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(embeddingEndpoint, request, String.class);
            JsonNode json = mapper.readTree(response.getBody());

            JsonNode vectorNode = json.at("/data/0/embedding");
            List<Double> embedding = mapper.convertValue(
                    vectorNode,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Double>>() {
                    });
            return embedding;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Double> getEmbedding(String input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "text-embedding-ada-002");
        body.put("input", input);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/embeddings",
                    request,
                    String.class);

            JsonNode json = mapper.readTree(response.getBody());
            JsonNode embedding = json.at("/data/0/embedding");

            return mapper.convertValue(embedding, new TypeReference<List<Double>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
