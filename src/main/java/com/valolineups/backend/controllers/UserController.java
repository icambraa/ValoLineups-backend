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

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario sincronizado",
                    "newUser", isNewUser,
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
