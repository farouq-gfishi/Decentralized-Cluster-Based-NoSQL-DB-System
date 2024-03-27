package com.atypon.nosql.affinitynode.controller;

import com.atypon.nosql.affinitynode.crud.DataBaseCRUD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AffinityNodeController {

    DataBaseCRUD dataBaseCRUD;

    @Autowired
    public AffinityNodeController(DataBaseCRUD dataBaseCRUD) {
        this.dataBaseCRUD = dataBaseCRUD;
    }

    @PostMapping("/create-db/{dbName}")
    public ResponseEntity<String> createDB(@PathVariable String dbName) {
        return dataBaseCRUD.createDB(dbName);
    }

    @PostMapping("/add-document/{dbName}/{documentName}")
    public ResponseEntity<String> addDocument(@PathVariable String documentName,
                                              @PathVariable String dbName,
                                              @RequestBody String documentContent) {
        return dataBaseCRUD.addDocument(documentName, dbName, documentContent);
    }

    @GetMapping("/get/{dbName}/{documentName}/{id}")
    public ResponseEntity<String> getDocumentById(@PathVariable String dbName,
                                                  @PathVariable String documentName,
                                                  @PathVariable String id) {
        return dataBaseCRUD.getDocumentById(dbName, documentName, id);
    }

    @GetMapping("/get-all/{dbName}/{documentName}")
    public ResponseEntity<String> getDocument(@PathVariable String dbName,
                                              @PathVariable String documentName) {
        return dataBaseCRUD.getDocument(dbName, documentName);
    }

    @PutMapping("/update/{dbName}/{documentName}/{id}")
    public ResponseEntity<String> updateDocumentById(@PathVariable String dbName,
                                                     @PathVariable String documentName,
                                                     @PathVariable String id,
                                                     @RequestBody Map<String,String> requestBody) throws InterruptedException {
        String updatedContent = requestBody.get("updatedContent");
        String currentContent = null;
        if(requestBody.containsKey("currentContent")) {
            currentContent = requestBody.get("currentContent");
        }
        return dataBaseCRUD.updateDocumentById(dbName, documentName, id, updatedContent, currentContent);
    }

    @DeleteMapping("/delete-db/{dbName}")
    public ResponseEntity<String> deleteDB(@PathVariable String dbName) {
        return dataBaseCRUD.deleteDB(dbName);
    }

    @DeleteMapping("/delete-document/{dbName}/{documentName}")
    public ResponseEntity<String> deleteDocument(@PathVariable String dbName,
                                                 @PathVariable String documentName) {
        return dataBaseCRUD.deleteDocument(dbName, documentName);
    }

    @DeleteMapping("/delete/{dbName}/{documentName}/{id}")
    public ResponseEntity<String> deleteDocumentById(@PathVariable String dbName,
                                                     @PathVariable String documentName,
                                                     @PathVariable String id) {
        return dataBaseCRUD.deleteDocumentById(dbName, documentName, id);
    }
}



