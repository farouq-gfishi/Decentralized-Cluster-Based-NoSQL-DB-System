package com.atypon.nosql.bootstrappingnode.service;

import com.atypon.nosql.bootstrappingnode.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class BootstrappingNodeService {

    private int currentNodeIndex = 0;
    private final List<String> nodes = List.of(
            "http://node1:8080/user/assign-user",
            "http://node2:8080/user/assign-user",
            "http://node3:8080/user/assign-user",
            "http://node4:8080/user/assign-user",
            "http://affinity-node-1:8080/user/assign-user",
            "http://affinity-node-2:8080/user/assign-user"
    );

    private final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/user";

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;


    public void startCluster() {
        distributeUser();
    }

    private void distributeUser() {
        List<User>users = getAllUsers();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for (User user : users) {
            executorService.submit(() -> {
                String selectedNode;
                synchronized (this) {
                    selectedNode = selectNodeForUser();
                }
                assignUserToNode(user, selectedNode);
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        File directory = new File(DATABASE_FOLDER_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                int numThreads = Runtime.getRuntime().availableProcessors();
                ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        executorService.submit(() -> {
                            User user = parseUserFromJsonFile(file);
                            if (user != null) {
                                synchronized (this) {
                                    users.add(user);
                                }
                            }
                        });
                    }
                }
                executorService.shutdown();
                try {
                    executorService.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
        return users;
    }

    private User parseUserFromJsonFile(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String selectNodeForUser() {
        String selectedNode = nodes.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % nodes.size();
        return selectedNode;
    }

    private void assignUserToNode(User user, String nodeUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(username, password);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", user.getId());
            jsonObject.put("name", user.getName());
            jsonObject.put("password", user.getPassword());
            jsonObject.put("role", user.getRole());
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(4), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(nodeUrl, HttpMethod.POST, request, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println(user.getName() + " assigned successfully to node " + nodeUrl);
            } else {
                System.err.println("Error assigning " + user.getName() + " to node " + nodeUrl + ": HTTP error code " + responseEntity.getStatusCodeValue());
            }
        } catch (Exception  e) {
            System.err.println("Error assigning " + user.getName() + " to node " + nodeUrl + ": " + e.getMessage());
        }
    }

    public ResponseEntity<String> addNewUser(User user) {
        try {
            user.setId(UUID.randomUUID().toString());
            String hashedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
            user.setPassword("{bcrypt}" + hashedPassword);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String userJson = objectMapper.writeValueAsString(user);
            String filePath = DATABASE_FOLDER_PATH + "/user_" + user.getId() + ".json";
            Files.write(Paths.get(filePath), userJson.getBytes());
            String selectedNode;
            synchronized (this) {
                selectedNode = selectNodeForUser();
            }
            assignUserToNode(user, selectedNode);
            return ResponseEntity.status(HttpStatus.OK).body(user.getName() + " assign to node: " + selectedNode);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error in assign user to node: " + e.getMessage());
        }
    }

}


