package com.atypon.nosql.bootstrappingnode.service;

import com.atypon.nosql.bootstrappingnode.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class BootstrappingNodeService {

    private static int currentNodeIndex = 0;
    private static List<String> nodes = List.of(
            "http://node1:8080/user/assign-user",
            "http://node2:8080/user/assign-user",
            "http://node3:8080/user/assign-user",
            "http://affinity-node:8080/user/assign-user"
    );

    private final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/user";


    public void startCluster() {
        distributeUser();
    }

    private void distributeUser() {
        List<User>users = getAllUsers();
        for(User user:users) {
            assignUserToNode(user, nodes.get(currentNodeIndex));
            currentNodeIndex = (currentNodeIndex + 1) % nodes.size();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        File directory = new File(DATABASE_FOLDER_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        User user = parseUserFromJsonFile(file);
                        if (user != null) {
                            users.add(user);
                        }
                    }
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

    private void assignUserToNode(User user, String nodeUrl) {
        try {
            URL url = new URL(nodeUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            String userJson = "{\"id\":\"" + user.getId() + "\",\"name\":\"" + user.getName() + "\",\"password\":\"" + user.getPassword() + "\"}";
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = userJson.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("User assigned successfully to node " + nodeUrl);
            } else {
                System.err.println("Error assigning user to node " + nodeUrl + ": HTTP error code " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Error assigning user to node " + nodeUrl + ": " + e.getMessage());
        }
    }

    public ResponseEntity<String> addUserAndAssignToNode(User user) {
        try {
            user.setId(UUID.randomUUID().toString());
            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(user);
            String filePath = DATABASE_FOLDER_PATH + "/user_" + user.getId() + ".json";
            Files.write(Paths.get(filePath), userJson.getBytes());
            String nodeUrl = nodes.get(currentNodeIndex);
            assignUserToNode(user, nodeUrl);
            currentNodeIndex = (currentNodeIndex + 1) % nodes.size();
            return ResponseEntity.status(HttpStatus.OK).body(user.getName() + " assign to node: " + nodeUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error in assign user to node: " + e.getMessage());
        }
    }


}


