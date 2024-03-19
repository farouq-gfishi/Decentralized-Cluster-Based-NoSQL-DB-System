package com.atypon.nosql.affinitynode.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final String DATABASE_FOLDER_PATH = System.getenv("DATABASE_FOLDER_PATH") + "/";


    @PostMapping("assign-user")
    public ResponseEntity<String> receivedUser(@RequestBody User user) {
        try {
            String documentFolderPath = DATABASE_FOLDER_PATH + "User";
            File documentFolder = new File(documentFolderPath);
            if (!documentFolder.exists()) {
                documentFolder.mkdirs();
            }
            String userJson = "{\"id\":\"" + user.getId() + "\",\"name\":\"" + user.getName() + "\",\"password\":\"" + user.getPassword() + "\"}";
            File userFile = new File(documentFolder, user.getId() + ".json");
            FileWriter writer = new FileWriter(userFile);
            writer.write(userJson);
            writer.close();
            return ResponseEntity.status(HttpStatus.OK).body("User assigned successfully with ID: " + user.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error assigning user: " + e.getMessage());
        }
    }
}

