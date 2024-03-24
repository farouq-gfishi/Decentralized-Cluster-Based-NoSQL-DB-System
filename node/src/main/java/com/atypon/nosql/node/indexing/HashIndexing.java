package com.atypon.nosql.node.indexing;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class HashIndexing {
    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";
    private Map<String, Map<String, String>> indexes;

    public HashIndexing() {
        indexes = new HashMap<>();
    }

    public void persistIndexes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASE_FOLDER_PATH + "index.txt"))) {
            for (Map.Entry<String, Map<String, String>> entry : indexes.entrySet()) {
                String documentName = entry.getKey();
                Map<String, String> indexMap = entry.getValue();
                for (Map.Entry<String, String> docEntry : indexMap.entrySet()) {
                    String docId = docEntry.getKey();
                    String fileName = docEntry.getValue();
                    writer.write(documentName + "," + docId + "," + fileName);
                    writer.newLine();
                }
            }

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
