package com.atypon.nosql.affinitynode.broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class BroadCast {

    private static final String WORKER_NODE_1 = "http://node1:8080";
    private static final String WORKER_NODE_2 = "http://node2:8080";
    private static final String WORKER_NODE_3 = "http://node3:8080";
    @Value("${app.username}")
    private String username;
    @Value("${app.password}")
    private String password;
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    @Autowired
    public BroadCast(RestTemplate restTemplate, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.headers = headers;
    }

    public void createDB(String dbName) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/create-db";
        sendRequest(WORKER_NODE_1 + endpoint, dbName);
        sendRequest(WORKER_NODE_2 + endpoint, dbName);
        sendRequest(WORKER_NODE_3 + endpoint, dbName);
    }

    public void addDocument(String dbName, String documentName, String documentContent, String uniqueId) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/add-document";
        sendRequest(WORKER_NODE_1 + endpoint, dbName, documentName, documentContent, uniqueId);
        sendRequest(WORKER_NODE_2 + endpoint, dbName, documentName, documentContent, uniqueId);
        sendRequest(WORKER_NODE_3 + endpoint, dbName, documentName, documentContent, uniqueId);
    }

    public void updateDocument(String dbName, String documentName, String documentContent, String uniqueId) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/update-document";
        sendRequest(WORKER_NODE_1 + endpoint, dbName, documentName, documentContent, uniqueId);
        sendRequest(WORKER_NODE_2 + endpoint, dbName, documentName, documentContent, uniqueId);
        sendRequest(WORKER_NODE_3 + endpoint, dbName, documentName, documentContent, uniqueId);
    }

    public void deleteDb(String dbName) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/delete-db";
        sendRequest(WORKER_NODE_1 + endpoint, dbName);
        sendRequest(WORKER_NODE_2 + endpoint, dbName);
        sendRequest(WORKER_NODE_3 + endpoint, dbName);
    }

    public void deleteDocument(String dbName, String documentName) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/delete-document";
        sendRequest(WORKER_NODE_1 + endpoint, dbName, documentName);
        sendRequest(WORKER_NODE_2 + endpoint, dbName, documentName);
        sendRequest(WORKER_NODE_3 + endpoint, dbName, documentName);
    }

    public void deleteDocumentById(String dbName, String documentName, String id) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint = "/broadcast/delete-document-by-id";
        sendRequest(WORKER_NODE_1 + endpoint, dbName, documentName, id);
        sendRequest(WORKER_NODE_2 + endpoint, dbName, documentName, id);
        sendRequest(WORKER_NODE_3 + endpoint, dbName, documentName, id);
    }

    private void sendRequest(String endpoint, String dbName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<String> requestEntity = new HttpEntity<>(dbName, headers);
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

    private void sendRequest(String endpoint, String dbName, String documentName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dbName", dbName);
            requestBody.put("documentName", documentName);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody,headers);
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

    private void sendRequest(String endpoint, String dbName, String documentName, String documentContent, String uniqueId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dbName", dbName);
            requestBody.put("documentName", documentName);
            requestBody.put("uniqueId", uniqueId);
            requestBody.put("documentContent", documentContent);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody,headers);
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

    private void sendRequest(String endpoint, String dbName, String documentName, String id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dbName", dbName);
            requestBody.put("documentName", documentName);
            requestBody.put("id", id);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
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
