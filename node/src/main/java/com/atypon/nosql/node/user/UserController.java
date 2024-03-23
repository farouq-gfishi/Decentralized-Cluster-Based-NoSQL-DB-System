package com.atypon.nosql.node.user;

import com.atypon.nosql.node.security.SecurityConfiguration;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";

    private SecurityConfiguration securityConfiguration;

    @Autowired
    public UserController(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    @PostMapping("assign-user")
    public ResponseEntity<String> receivedUser(@RequestBody User user) {
        try {
            String documentFolderPath = DATABASE_FOLDER_PATH + "User";
            File documentFolder = new File(documentFolderPath);
            if (!documentFolder.exists()) {
                documentFolder.mkdirs();
            }
            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            userJson.put("name", user.getName());
            userJson.put("password", user.getPassword());
            userJson.put("role", user.getRole());
            File userFile = new File(documentFolder, user.getId() + ".json");
            try (FileWriter writer = new FileWriter(userFile)) {
                writer.write(userJson.toString(4));
            }
            securityConfiguration.addUserToMemory(user);
            return ResponseEntity.status(HttpStatus.OK).body("User assigned successfully with ID: " + user.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error assigning user: " + e.getMessage());
        }
    }
}
