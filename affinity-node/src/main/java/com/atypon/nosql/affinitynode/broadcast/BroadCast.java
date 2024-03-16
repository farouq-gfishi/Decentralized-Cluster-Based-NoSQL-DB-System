package com.atypon.nosql.affinitynode.broadcast;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BroadCast {

    private static final String WORKER_NODE_1 = "http://node1:8080";
    private static final String WORKER_NODE_2 = "http://node2:8080";
    private static final String WORKER_NODE_3 = "http://node3:8080";

    public void broadcast(Map<String, Map<String, String>> indexes) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/loadIndexes";
        sendRequest(restTemplate, WORKER_NODE_1 + endpoint, indexes);
        sendRequest(restTemplate, WORKER_NODE_2 + endpoint, indexes);
        sendRequest(restTemplate, WORKER_NODE_3 + endpoint, indexes);
    }

    private void sendRequest(RestTemplate restTemplate, String endpoint, Map<String, Map<String, String>> indexes) {
        try {
            HttpEntity<Map<String, Map<String, String>>> requestEntity = new HttpEntity<>(indexes);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Request successful to: " + endpoint);
            } else {
                System.err.println("Failed to send request to: " + endpoint);
            }
        } catch (Exception e) {
            System.err.println("Error sending request to: " + endpoint);
            e.printStackTrace();
        }
    }
}
