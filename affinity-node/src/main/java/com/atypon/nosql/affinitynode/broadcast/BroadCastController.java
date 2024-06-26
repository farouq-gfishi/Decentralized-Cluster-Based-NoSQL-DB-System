package com.atypon.nosql.affinitynode.broadcast;

import com.atypon.nosql.affinitynode.indexing.HashIndexing;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/broadcast")
public class BroadCastController {

    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private HashIndexing hashIndexing;

    @Autowired
    public BroadCastController(HashIndexing hashIndexing) {
        this.hashIndexing = hashIndexing;
    }

    @PostMapping("/create-db")
    public void createDB(@RequestBody String dbName) {
        String dbFolderPath = DATABASE_FOLDER_PATH + dbName;
        File dbFolder = new File(dbFolderPath);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }
    }

    @PostMapping("/add-document")
    public void addDocument(@RequestBody Map<String,String> requestBody) {
        try {
            String dbName = requestBody.get("dbName");
            String documentName = requestBody.get("documentName");
            String uniqueId = requestBody.get("uniqueId");
            String documentContent = requestBody.get("documentContent");

            String dbFolderPath = DATABASE_FOLDER_PATH + dbName + "/";
            String documentFolderPath = dbFolderPath + documentName + "/";
            String jsonFileName = documentName + "_" + uniqueId + ".json";
            String jsonFilePath = documentFolderPath + jsonFileName;
            File dbFolder = new File(dbFolderPath);
            if (!dbFolder.exists()) {
                dbFolder.mkdirs();
            }
            File documentFolder = new File(documentFolderPath);
            if (!documentFolder.exists()) {
                documentFolder.mkdirs();
            }
            JSONObject jsonObject = new JSONObject(documentContent);
            jsonObject.put("id", uniqueId);
            String updatedDocumentContent = jsonObject.toString(4);
            Files.write(Paths.get(jsonFilePath), updatedDocumentContent.getBytes());
            if (!hashIndexing.getIndexes().containsKey(dbName + "-" +documentName)) {
                hashIndexing.createIndexMap(dbName,documentName);
            }
            hashIndexing.addToIndex(dbName, documentName, uniqueId, jsonFileName);
            hashIndexing.persistIndexes();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error adding document.");
        }
    }

    @PostMapping("/update-document")
    public void updateDocument(@RequestBody Map<String,String> requestBody) {
        String dbName = requestBody.get("dbName");
        String documentName = requestBody.get("documentName");
        String uniqueId = requestBody.get("uniqueId");
        String documentContent = requestBody.get("documentContent");
        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        String fileName = index.get(uniqueId);
        File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
        try {
            JSONObject jsonObject = new JSONObject(documentContent);
            jsonObject.put("id", uniqueId);
            String updatedJsonContent = jsonObject.toString(4);
            Files.write(jsonFile.toPath(), updatedJsonContent.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete-db")
    public void deleteDb(@RequestBody Map<String,String> requestBody) {
        String dbName = requestBody.get("dbName");
        String dbFolderPath = DATABASE_FOLDER_PATH + dbName;
        File dbFolder = new File(dbFolderPath);
        if (dbFolder.exists()) {
            File[] documentFolders = dbFolder.listFiles();
            if (documentFolders != null) {
                for (File documentFolder : documentFolders) {
                    String documentName = documentFolder.getName();
                    Map<String,String> map = new HashMap<>();
                    map.put("dbName", dbName);
                    map.put("documentName", documentName);
                    deleteDocument(map);
                }
            }
            dbFolder.delete();
        }
    }

    @PostMapping("/delete-document")
    public void deleteDocument(@RequestBody Map<String,String> requestBody) {
        String dbName = requestBody.get("dbName");
        String documentName = requestBody.get("documentName");

        String documentFolderPath = DATABASE_FOLDER_PATH + dbName + "/" + documentName;
        File documentFolder = new File(documentFolderPath);
        if (documentFolder.exists()) {
            if (deleteFolder(documentFolder)) {
                hashIndexing.getIndexes().remove(dbName + "-" + documentName);
                hashIndexing.persistIndexes();
            }
        }
    }

    @PostMapping("/delete-document-by-id")
    public void deleteDocumentById(@RequestBody Map<String,String> requestBody) {
        String dbName = requestBody.get("dbName");
        String documentName = requestBody.get("documentName");
        String id = requestBody.get("id");

        Map<String, String> index = hashIndexing.getIndexes().get(dbName+"-"+documentName);
        String fileName = index.get(id);
        File jsonFile = new File(DATABASE_FOLDER_PATH + dbName + "/" + documentName + "/" + fileName);
        if (jsonFile.exists()) {
            if (jsonFile.delete()) {
                index.remove(id);
                hashIndexing.persistIndexes();
            }
        }
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

