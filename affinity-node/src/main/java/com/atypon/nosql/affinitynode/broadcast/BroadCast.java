package com.atypon.nosql.affinitynode.broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BroadCast {

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    private final String[] WORKER_NODES = {
            "http://node1:8080/broadcast",
            "http://node2:8080/broadcast",
            "http://node3:8080/broadcast",
            "http://node4:8080/broadcast",
            "http://affinity-node-1:8080/broadcast",
            "http://affinity-node-2:8080/broadcast"
    };
    private ExecutorService executorService;
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    @Autowired
    public BroadCast(RestTemplate restTemplate, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.headers = headers;
    }

    public void createDB(String dbName) {
        executeTasks("/create-db", dbName);
    }

    public void addDocument(String dbName, String documentName, String documentContent, String uniqueId) {
        executeTasks("/add-document", dbName, documentName, documentContent, uniqueId);
    }

    public void updateDocument(String dbName, String documentName, String documentContent, String uniqueId) {
        executeTasks("/update-document", dbName, documentName, documentContent, uniqueId);
    }

    public void deleteDb(String dbName) {
        executeTasks("/delete-db", dbName);
    }

    public void deleteDocument(String dbName, String documentName) {
        executeTasks("/delete-document", dbName, documentName);
    }

    public void deleteDocumentById(String dbName, String documentName, String id) {
        executeTasks("/delete-document-by-id", dbName, documentName, id);
    }

    private void executeTasks(String endpoint, Object... params) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        executorService = Executors.newFixedThreadPool(WORKER_NODES.length);
        for (String workerNode : WORKER_NODES) {
            Runnable task = () -> sendRequest(workerNode + endpoint, params);
            executorService.submit(task);
        }

        executorService.shutdown();
    }

    private void sendRequest(String endpoint, Object... params) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            String dbName = (String) params[0];
            if (endpoint.endsWith("/create-db") || endpoint.endsWith("/delete-db")) {
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("dbName", dbName);
                sendRequestWithBody(endpoint, headers, requestBody);
            } else if (endpoint.endsWith("/add-document") || endpoint.endsWith("/update-document")) {
                String documentName = (String) params[1];
                String documentContent = (String) params[2];
                String uniqueId = (String) params[3];
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("dbName", dbName);
                requestBody.put("documentName", documentName);
                requestBody.put("documentContent", documentContent);
                requestBody.put("uniqueId", uniqueId);
                sendRequestWithBody(endpoint, headers, requestBody);
            } else if (endpoint.endsWith("/delete-document")) {
                String documentName = (String) params[1];
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("dbName", dbName);
                requestBody.put("documentName", documentName);
                sendRequestWithBody(endpoint, headers, requestBody);
            } else if (endpoint.endsWith("/delete-document-by-id")) {
                String documentName = (String) params[1];
                String id = (String) params[2];
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("dbName", dbName);
                requestBody.put("documentName", documentName);
                requestBody.put("id", id);
                sendRequestWithBody(endpoint, headers, requestBody);
            } else {
                System.err.println("Unsupported endpoint: " + endpoint);
            }
        } catch (Exception e) {
            System.err.println("Error sending request to: " + endpoint);
            e.printStackTrace();
        }
    }

    private void sendRequestWithBody(String endpoint, HttpHeaders headers, Map<String, String> requestBody) {
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Request successful to: " + endpoint);
        } else {
            System.err.println("Failed to send request to: " + endpoint);
        }
    }
}
