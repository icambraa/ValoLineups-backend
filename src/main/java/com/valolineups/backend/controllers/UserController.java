package com.valolineups.backend.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.valolineups.backend.models.User;
import com.valolineups.backend.repositories.UserRepository;
import com.valolineups.backend.services.FirebaseService;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final FirebaseService firebaseService;
    private final UserRepository userRepository;

    public UserController(FirebaseService firebaseService, UserRepository userRepository) {
        this.firebaseService = firebaseService;
        this.userRepository = userRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String idToken = authHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = firebaseService.verifyToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = (String) decodedToken.getClaims().get("name");
            String photoUrl = (String) decodedToken.getClaims().get("picture");

            boolean isNewUser = false;
            User user;

            Optional<User> existingUserOpt = userRepository.findById(uid);
            if (existingUserOpt.isEmpty()) {
                user = new User();
                user.setFirebaseUid(uid);
                user.setEmail(email);
                user.setDisplayName(name);
                user.setPhotoUrl(photoUrl);
                userRepository.save(user);
                isNewUser = true;
                System.out.println("✅ Usuario nuevo sincronizado: " + uid);
            } else {
                user = existingUserOpt.get();
            }

            String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Sin nombre";
            String photoUrlToReturn = user.getPhotoUrl() != null ? user.getPhotoUrl() : "Sin foto";
            String nickname = user.getNickname() != null ? user.getNickname() : "Sin apodo";

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario sincronizado",
                    "newUser", isNewUser,
                    "user", Map.of(
                            "firebaseUid", user.getFirebaseUid(),
                            "email", user.getEmail(),
                            "displayName", displayName,
                            "photoUrl", photoUrlToReturn,
                            "nickname", nickname,
                            "isAdmin", user.isAdmin())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Token inválido");
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> updates) {
        try {
            String idToken = authHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = firebaseService.verifyToken(idToken);

            String uid = decodedToken.getUid();
            Optional<User> userOpt = userRepository.findById(uid);

            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            if (updates.containsKey("nickname")) {
                user.setNickname(updates.get("nickname"));
            }
            if (updates.containsKey("displayName")) {
                user.setDisplayName(updates.get("displayName"));
            }
            if (updates.containsKey("photoUrl")) {
                user.setPhotoUrl(updates.get("photoUrl"));
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Perfil actualizado",
                    "user", Map.of(
                            "firebaseUid", user.getFirebaseUid(),
                            "email", user.getEmail(),
                            "displayName", user.getDisplayName(),
                            "photoUrl", user.getPhotoUrl(),
                            "nickname", user.getNickname(),
                            "isAdmin", user.isAdmin())));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Token inválido");
        }
    }

}
