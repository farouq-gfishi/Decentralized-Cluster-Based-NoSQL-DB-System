package com.atypon.nosql.affinitynode.crud;

import org.springframework.http.ResponseEntity;

public interface DataBaseCRUD {

    ResponseEntity<String> createDB(String dbName);

    ResponseEntity<String> addDocument(String documentName, String dbName, String documentContent);

    ResponseEntity<String> getDocumentById(String dbName, String documentName, String id);

    ResponseEntity<String> getDocument(String dbName, String documentName);

    ResponseEntity<String> updateDocumentById(String dbName, String documentName, String id, String updatedContent, String currentContent) throws InterruptedException;

    ResponseEntity<String> deleteDB(String dbName) ;

    ResponseEntity<String> deleteDocument(String dbName, String documentName);

    ResponseEntity<String> deleteDocumentById(String dbName, String documentName, String id);

}

