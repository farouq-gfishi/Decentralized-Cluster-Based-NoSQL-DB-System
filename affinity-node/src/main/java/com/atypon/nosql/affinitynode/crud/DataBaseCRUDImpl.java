package com.atypon.nosql.affinitynode.crud;

import com.atypon.nosql.affinitynode.broadcast.BroadCast;
import com.atypon.nosql.affinitynode.indexing.HashIndexing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private ObjectMapper objectMapper;
    private HashIndexing hashIndexing;
    private BroadCast broadCast;

    @Autowired
    public DataBaseCRUDImpl(ObjectMapper objectMapper, HashIndexing hashIndexing, BroadCast broadCast) {
        this.objectMapper = objectMapper;
        this.hashIndexing = hashIndexing;
        this.broadCast = broadCast;
        hashIndexing.loadIndexes();
    }

    @Override
    public ResponseEntity<String> createDB(String dbName) {
        String dbFolderPath = DATABASE_FOLDER_PATH + dbName;
        File dbFolder = new File(dbFolderPath);
        if (!dbFolder.exists()) {
            if (dbFolder.mkdir()) {
                broadCast.createDB(dbName);
                return ResponseEntity.status(HttpStatus.OK).body("Database '" + dbName + "' created successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to create database folder '" + dbName + "'.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Database '" + dbName + "' already exists.");
        }
    }

    @Override
    public ResponseEntity<String> addDocument(String documentName, String dbName, String documentContent) {
        String documentFolderPath = DATABASE_FOLDER_PATH + dbName + "/" + documentName;
        File documentFolder = new File(documentFolderPath);
        try {
            if (!documentFolder.exists()) {
                if (documentFolder.mkdirs()) {
                    helperAddDocument(documentName, dbName, documentContent, documentFolderPath);
                    return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' added successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to create document folder '" + documentName + "'.");
                }
            } else {
                helperAddDocument(documentName, dbName, documentContent, documentFolderPath);
                return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' added successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to create JSON object in document folder '" + documentName + "'.");
        }
    }
    private void helperAddDocument(String documentName, String dbName, String documentContent, String documentFolderPath) throws IOException {
        String uniqueId = UUID.randomUUID().toString();
        JsonNode jsonObject = objectMapper.readTree(documentContent);
        ((ObjectNode) jsonObject).put("id", uniqueId);
        String updatedDocumentContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        String jsonFileName = documentName + "_" + uniqueId + ".json";
        String jsonFilePath = documentFolderPath + "/" + jsonFileName;
        Files.write(Paths.get(jsonFilePath), updatedDocumentContent.getBytes());
        if (!hashIndexing.getIndexes().containsKey(dbName + "-" +documentName)) {
            hashIndexing.createIndexMap(dbName,documentName);
        }
        hashIndexing.addToIndex(dbName, documentName, uniqueId, jsonFileName);
        hashIndexing.persistIndexes();
        broadCast.addDocument(dbName, documentName, documentContent, uniqueId);
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
        if (dbName == null || dbName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No database selected. Please use the '/use' endpoint to select a database first.");
        }
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
    public ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, String updatedContent) {
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        if (index == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document '" + documentName + "' not found.");
        }
        String fileName = index.get(id);
        if (fileName != null) {
            File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
            if (jsonFile.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(updatedContent);
                    ((ObjectNode) jsonNode).put("id", id);
                    String updatedJsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                    Files.write(jsonFile.toPath(), updatedJsonContent.getBytes());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                broadCast.updateDocument(dbName, documentName, updatedContent, id);
                return ResponseEntity.status(HttpStatus.OK).body("Document with ID '" + id + "' updated successfully for document '" + documentName + "'.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON file '" + fileName + "' not found for document '" + documentName + "'.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON with ID '" + id + "' not found in document '" + documentName + "'.");
    }

    @Override
    public ResponseEntity<String> deleteDB(String dbName) {
        String dbFolderPath = DATABASE_FOLDER_PATH + dbName;
        File dbFolder = new File(dbFolderPath);
        if (dbFolder.exists()) {
            File[] documentFolders = dbFolder.listFiles();
            if (documentFolders != null) {
                for (File documentFolder : documentFolders) {
                    String documentName = documentFolder.getName();
                    deleteDocument(dbName, documentName);
                }
            }
            if (dbFolder.delete()) {
                broadCast.deleteDb(dbName);
                return ResponseEntity.status(HttpStatus.OK).body("Database '" + dbName + "' deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to delete database folder '" + dbName + "'.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Database '" + dbName + "' does not exist.");
        }
    }

    @Override
    public ResponseEntity<String> deleteDocument(String dbName, String documentName) {
        String documentFolderPath = DATABASE_FOLDER_PATH + dbName + "/" + documentName;
        File documentFolder = new File(documentFolderPath);
        if (documentFolder.exists()) {
            if (deleteFolder(documentFolder)) {
                hashIndexing.getIndexes().remove(dbName + "-" + documentName);
                hashIndexing.persistIndexes();
                broadCast.deleteDocument(dbName, documentName);
                return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to delete document '" + documentName + "'.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document '" + documentName + "' not found.");
        }
    }

    @Override
    public ResponseEntity<String> deleteDocumentById(String dbName, String documentName, String id) {
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        if (index == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document '" + documentName + "' not found.");
        }
        String fileName = index.get(id);
        if (fileName != null) {
            File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
            if (jsonFile.exists()) {
                if (jsonFile.delete()) {
                    index.remove(id);
                    hashIndexing.persistIndexes();
                    broadCast.deleteDocumentById(dbName, documentName, id);
                    return ResponseEntity.status(HttpStatus.OK).body("Document '" + documentName + "' with ID '" + id + "' deleted successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete JSON file '" + fileName + "' for document '" + documentName + "'.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON file '" + fileName + "' not found for document '" + documentName + "'.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JSON with ID '" + id + "' not found in document '" + documentName + "'.");
    }

    private boolean deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        return folder.delete();
    }
}
