package com.atypon.nosql.affinitynode.crud;

import com.atypon.nosql.affinitynode.broadcast.BroadCast;
import com.atypon.nosql.affinitynode.indexing.HashIndexing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataBaseCRUDImpl implements DataBaseCRUD {

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    private final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private HashIndexing hashIndexing;
    private BroadCast broadCast;

    @Autowired
    public DataBaseCRUDImpl(HashIndexing hashIndexing, BroadCast broadCast) {
        this.hashIndexing = hashIndexing;
        this.broadCast = broadCast;
        hashIndexing.loadIndexes();
    }

    @Override
    public ResponseEntity<String> createDB(String dbName) {
        broadCast.createDB(dbName);
        return ResponseEntity.status(HttpStatus.OK).body("Database '" + dbName + "' created successfully.");
    }

    @Override
    public ResponseEntity<String> addDocument(String documentName, String dbName, String documentContent) {
        String uniqueId = UUID.randomUUID().toString();
        broadCast.addDocument(dbName, documentName, documentContent, uniqueId);
        return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' added successfully.");
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
    public ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, Map<String,String> requestBody) {
        String documentContent = null;
        if(requestBody.containsKey("currentContent")) {
            String nodeName = requestBody.get("nodeName");
            String currentContent = requestBody.get("currentContent");
            String updatedContent = requestBody.get("updatedContent");
            synchronized (this) {
                if (!currentContent.equals(Integer.toString(getDocumentForUpdate(dbName, documentName, id).hashCode()))) {
                    String affinityNodeEndpoint = "http://" + nodeName + ":8080/api" + "/update/" + dbName + "/" + documentName + "/" + id;
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBasicAuth(username, password);
                    HttpEntity<String> requestEntity = new HttpEntity<>(updatedContent, headers);
                    System.out.println("Redirect to " + nodeName + "::race condition");
                    ResponseEntity<String> responseEntity = restTemplate.exchange(affinityNodeEndpoint, HttpMethod.PUT, requestEntity, String.class);
                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
                        return ResponseEntity.ok("Redirect::race condition");
                    }
                }
            }

        }
        if(requestBody.containsKey("updatedContent")) {
            documentContent = requestBody.get("updatedContent");
        }
        else {
            documentContent = convertToJSONString(requestBody.toString());
        }
        broadCast.updateDocument(dbName, documentName, documentContent, id);
        return ResponseEntity.status(HttpStatus.OK).body("Document with ID '" + id + "' updated successfully for document '" + documentName + "'.");
    }

    @Override
    public ResponseEntity<String> deleteDB(String dbName) {
        broadCast.deleteDb(dbName);
        return ResponseEntity.status(HttpStatus.OK).body("Database '" + dbName + "' deleted successfully.");
    }

    @Override
    public ResponseEntity<String> deleteDocument(String dbName, String documentName) {
        broadCast.deleteDocument(dbName, documentName);
        return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' deleted successfully.");
    }

    @Override
    public ResponseEntity<String> deleteDocumentById(String dbName, String documentName, String id) {
        broadCast.deleteDocumentById(dbName, documentName, id);
        return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' with ID '" + id + "' deleted successfully.");
    }

    private String getDocumentForUpdate(String dbName, String documentName, String id) {
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

    private static String convertToJSONString(String content) {
        String jsonString = content.replaceAll("\\{", "{\"").replaceAll(",\\s*(?!\\})", "\",\"").replaceAll("=", "\":\"").replaceAll("\\}", "\"}");
        return jsonString;
    }
}
