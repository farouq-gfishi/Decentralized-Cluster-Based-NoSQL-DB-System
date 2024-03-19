package com.atypon.nosql.bootstrappingnode;

import com.atypon.nosql.bootstrappingnode.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Service
public class BootstrappingNode {

    private final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/user";


    public void startCluster() {
        distributeUser();
    }

    private void distributeUser() {
        List<String>nodes = List.of(
                "http://node1:8080/user/assign-user",
                "http://node2:8080/user/assign-user",
                "http://node3:8080/user/assign-user",
                "http://affinity-node:8080/user/assign-user"
        );
        List<User>users = readUsersFromJsonFiles();
        int numNodes = nodes.size();
        int numUsers = users.size();
        int usersPerNode = numUsers / numNodes;
        int remainingUsers = numUsers % numNodes;
        int userIndex = 0;
        for (String node : nodes) {
            int usersToSend = usersPerNode + (remainingUsers-- > 0 ? 1 : 0);
            for (int i = 0; i < usersToSend; i++) {
                User user = users.get(userIndex++);
                assignUserToNode(user, node);
            }
        }
    }

    private List<User> readUsersFromJsonFiles() {
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



}


