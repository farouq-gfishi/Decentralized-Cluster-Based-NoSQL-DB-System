package com.atypon.nosql.node.controller;

import com.atypon.nosql.node.crud.DataBaseCRUD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NodeController {

    private DataBaseCRUD dataBaseCRUD;

    @Autowired
    public void NodeController(DataBaseCRUD dataBaseCRUD) {
        this.dataBaseCRUD = dataBaseCRUD;
    }

    @PostMapping("/create-db/{dbName}")
    public ResponseEntity<String> createDB(@PathVariable String dbName) {
        return dataBaseCRUD.createDB(dbName);
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

    @PostMapping("/add-document/{dbName}/{documentName}")
    public ResponseEntity<String> addDocument(@PathVariable String dbName,
                                              @PathVariable String documentName,
                                              @RequestBody String documentContent) {
        return dataBaseCRUD.addDocument(dbName, documentName, documentContent);
    }

    @PutMapping("/update/{dbName}/{documentName}/{id}")
    public ResponseEntity<String> updateDocumentById(@PathVariable String dbName,
                                                     @PathVariable String documentName,
                                                     @PathVariable String id,
                                                     @RequestBody String updatedContent) {
        return dataBaseCRUD.updateDocumentById(dbName, documentName, id, updatedContent);
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


