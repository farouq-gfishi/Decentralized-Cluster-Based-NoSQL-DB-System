package com.atypon.nosql.node.crud;

import com.atypon.nosql.node.indexing.HashIndexing;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DataBaseCRUDImpl implements DataBaseCRUD {

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    private final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private final String AFFINITY_NODE_URL = System.getenv("AFFINITY");
    private HashIndexing hashIndexing;

    @Autowired
    public DataBaseCRUDImpl(HashIndexing hashIndexing) {
        this.hashIndexing = hashIndexing;
        hashIndexing.loadIndexes();
    }

    @Override
    public ResponseEntity<String> getDocumentById(String dbName, String documentName, String id) {
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        if (index == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document '" + documentName + "' not found.");
        }
        String fileName = index.get(id);
        if (fileName != null) {
            File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
            if (jsonFile.exists()) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
                    return ResponseEntity.ok(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON file '" + fileName + "' not found for document '" + documentName + "'.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON with ID '" + id + "' not found in document '" + documentName + "'.");
    }

    @Override
    public ResponseEntity<String> getDocument(String dbName, String documentName) {
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        if (index == null || index.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document '" + documentName + "' not found.");
        }
        List<String> jsonContents = new ArrayList<>();
        for (String fileName : index.values()) {
            File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
            if (jsonFile.exists()) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
                    jsonContents.add(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ResponseEntity.ok(jsonContents.toString());
    }

    @Override
    public ResponseEntity<String> createDB(String dbName) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/create-db/" + dbName;
        return invokeAffinityNodeEndpoint(HttpMethod.POST,
                affinityNodeEndpoint, null,
                "Database '" + dbName + "' created successfully.");
    }

    @Override
    public ResponseEntity<String> addDocument(String dbName, String documentName, String documentContent) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/add-document/" + dbName + "/" + documentName;
        return invokeAffinityNodeEndpoint(HttpMethod.POST,
                affinityNodeEndpoint, documentContent,
                "Document '" + documentName + "' added successfully.");
    }

    @Override
    public ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, String updatedContent) {
        String currentContent = Integer.toString(getDocument(dbName, documentName, id).hashCode());
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/update/" + dbName + "/" + documentName + "/" + id;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("currentContent", currentContent);
        requestBody.put("updatedContent", updatedContent);
        requestBody.put("nodeName", System.getenv("NODE_NAME"));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(affinityNodeEndpoint, HttpMethod.PUT, requestEntity, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).body(documentName + " with id: " + id + " is updated");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to invoke affinity-node endpoint: " + affinityNodeEndpoint);
        }
    }

    private String getDocument(String dbName, String documentName, String id) {
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        String fileName = index.get(id);
        if (fileName != null) {
            File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
            if (jsonFile.exists()) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())));
                    return (content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<String> deleteDB(String dbName) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/delete-db/" + dbName;
        return invokeAffinityNodeEndpoint(HttpMethod.DELETE,
                affinityNodeEndpoint, null,
                "Database '" + dbName + "' deleted successfully.");
    }

    @Override
    public ResponseEntity<String> deleteDocument(String dbName, String documentName) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/delete-document/" + dbName + "/" + documentName;
        return invokeAffinityNodeEndpoint(HttpMethod.DELETE,
                affinityNodeEndpoint, null,
                "Document '" + documentName + "' deleted successfully.");
    }

    @Override
    public ResponseEntity<String> deleteDocumentById(String dbName, String documentName, String id) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/delete/" + dbName + "/" + documentName + "/" + id;
        return invokeAffinityNodeEndpoint(HttpMethod.DELETE,
                affinityNodeEndpoint, null,
                "Document '" + documentName + "' with ID '" + id + "' deleted successfully.");
    }

    private ResponseEntity<String> invokeAffinityNodeEndpoint(HttpMethod method, String endpoint, String requestBody, String successMessage) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(endpoint, method, requestEntity, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(successMessage);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to invoke affinity-node endpoint: " + endpoint);
        }
    }
}

