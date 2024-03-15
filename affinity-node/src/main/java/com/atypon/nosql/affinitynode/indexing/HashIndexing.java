package com.atypon.nosql.affinitynode.indexing;

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

    public void loadIndexes() {
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

                    indexes.computeIfAbsent(documentName, k -> new HashMap<>()).put(docId, fileName);
                }
                System.out.println(indexes);
            } catch (IOException e) {
                System.err.println("Failed to load indexes from file: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Failed to create index.txt file: " + e.getMessage());
        }
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

    public void createIndexMap(String documentName) {
        indexes.put(documentName, new HashMap<>());
    }

    public void addToIndex(String documentName, String docId, String fileName) {
        Map<String, String> index = indexes.get(documentName);
        index.put(docId, fileName);
    }

    public Map<String, Map<String, String>> getIndexes() {
        return indexes;
    }
}

