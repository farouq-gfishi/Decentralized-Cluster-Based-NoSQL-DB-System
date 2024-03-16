package com.atypon.nosql.node.indexing;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class HashIndexing {
    private Map<String, Map<String, String>> indexes;

    public HashIndexing() {
        indexes = new HashMap<>();
    }

    public Map<String, Map<String, String>> getIndexes() {
        return indexes;
    }

    public void setIndexes(Map<String, Map<String, String>> indexes) {
        this.indexes = indexes;
    }
}
