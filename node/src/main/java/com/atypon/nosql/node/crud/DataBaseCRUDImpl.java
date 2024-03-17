package com.atypon.nosql.node.crud;

import com.atypon.nosql.node.indexing.HashIndexing;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DataBaseCRUDImpl implements DataBaseCRUD {
    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private static final String AFFINITY_NODE_URL = "http://affinity-node:8080";
    private ObjectMapper objectMapper;
    private HashIndexing hashIndexing;

    @Autowired
    public DataBaseCRUDImpl(ObjectMapper objectMapper, HashIndexing hashIndexing) {
        this.objectMapper = objectMapper;
        this.hashIndexing = hashIndexing;
        loadIndexesFromFile();
    }

    public void loadIndexesFromFile() {
        try {
            File indexFile = new File(DATABASE_FOLDER_PATH + "/index.txt");
            if (!indexFile.exists()) {
                if (indexFile.createNewFile()) {
                    System.out.println("index.txt file created successfully.");
                } else {
                    System.err.println("Failed to create index.txt file.");
                    return;
                }
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    String documentName = parts[0];
                    String docId = parts[1];
                    String fileName = parts[2];
                    hashIndexing.getIndexes().computeIfAbsent(documentName, k -> new HashMap<>()).put(docId, fileName);
                }
            } catch (IOException e) {
                System.err.println("Failed to load indexes from file: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Failed to create index.txt file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> createDB(String dbName) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/create-db/" + dbName;
        return invokeAffinityNodeEndpoint(HttpMethod.POST,
                affinityNodeEndpoint, null,
                "Database '" + dbName + "' created successfully.");
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
    public ResponseEntity<String> addDocument(String dbName, String documentName, String documentContent) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/add-document/" + dbName + "/" + documentName;
        return invokeAffinityNodeEndpoint(HttpMethod.POST,
                affinityNodeEndpoint, documentContent,
                "Document '" + documentName + "' added successfully.");
    }

    @Override
    public ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, String updatedContent) {
        String affinityNodeEndpoint = AFFINITY_NODE_URL + "/update/" + dbName + "/" + documentName + "/" + id;
        return invokeAffinityNodeEndpoint(HttpMethod.PUT,
                affinityNodeEndpoint, updatedContent,
                "Document '" + documentName + "' updated successfully.");
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
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(endpoint, method, requestEntity, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(successMessage);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to invoke affinity-node endpoint: " + endpoint);
        }
    }
}

