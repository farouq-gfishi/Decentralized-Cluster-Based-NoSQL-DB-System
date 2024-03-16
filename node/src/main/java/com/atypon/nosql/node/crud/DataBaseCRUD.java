package com.atypon.nosql.node.crud;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface DataBaseCRUD {

    void loadIndexes(Map<String, Map<String, String>> indexes);

    ResponseEntity<String> createDB(String dbName);

    ResponseEntity<String> addDocument(String documentName, String dbName, String documentContent);

    ResponseEntity<String> getDocumentById(String dbName, String documentName, String id);

    ResponseEntity<String> getDocument(String dbName, String documentName);

    ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, String updatedContent);

    ResponseEntity<String> deleteDB(String dbName) ;

    ResponseEntity<String> deleteDocument(String dbName, String documentName);

    ResponseEntity<String> deleteDocumentById(String dbName, String documentName, String id);

}
