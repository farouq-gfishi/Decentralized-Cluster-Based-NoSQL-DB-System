package com.atypon.nosql.node.indexing;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class HashIndexing {


    private String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";

    private Map<String, Map<String, String>> indexes = new HashMap<>();

    public void loadIndexes() {
        try {
            File indexFile = new File(DATABASE_FOLDER_PATH + "/index.json");
            if (!indexFile.exists()) {
                if (indexFile.createNewFile()) {
                    System.out.println("index.json file created successfully.");
                } else {
                    System.err.println("Failed to create index.json file.");
                    return;
                }
            }
            if (indexFile.length() == 0) {
                return;
            }
            String jsonString = new String(Files.readAllBytes(indexFile.toPath()));
            JSONObject jsonObject = new JSONObject(jsonString);
            for (String key : jsonObject.keySet()) {
                Map<String, String> indexMap = new HashMap<>();
                JSONObject innerJsonObject = jsonObject.getJSONObject(key);
                for (String innerKey : innerJsonObject.keySet()) {
                    indexMap.put(innerKey, innerJsonObject.getString(innerKey));
                }
                indexes.put(key, indexMap);
            }

        } catch (IOException e) {
            System.err.println("Failed to load indexes from file: " + e.getMessage());
        }
    }

    public void persistIndexes() {
        try {
            JSONObject jsonObject = new JSONObject(indexes);
            Files.write(Paths.get(DATABASE_FOLDER_PATH + "/index.json"), jsonObject.toString(4).getBytes());
        } catch (IOException e) {
            System.err.println("Failed to persist indexes to file: " + e.getMessage());
        }
    }

    public void createIndexMap(String dbName, String documentName) {
        indexes.put(dbName + "-" + documentName, new HashMap<>());
    }

    public void addToIndex(String dbName, String documentName, String docId, String fileName) {
        Map<String, String> index = indexes.get(dbName + "-" + documentName);
        index.put(docId, fileName);
    }

    public Map<String, Map<String, String>> getIndexes() {
        return indexes;
    }


}
