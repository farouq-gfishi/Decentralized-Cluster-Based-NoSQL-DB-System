package com.atypon.nosql.bootstrappingnode.controller;

import com.atypon.nosql.bootstrappingnode.entity.User;
import com.atypon.nosql.bootstrappingnode.service.BootstrappingNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bootstrapping-node")
public class BootstrappingNodeController {

    BootstrappingNodeService bootstrappingNodeService;

    @Autowired
    public BootstrappingNodeController(BootstrappingNodeService bootstrappingNodeService) {
        this.bootstrappingNodeService = bootstrappingNodeService;
    }

    @GetMapping("/start-cluster")
    public ResponseEntity<String> startCluster() {
        bootstrappingNodeService.startCluster();
        return ResponseEntity.status(HttpStatus.OK).body("Users are successfully assigned to nodes.");
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addNewUser(@RequestBody User user) {
        return bootstrappingNodeService.addNewUser(user);
    }
}
