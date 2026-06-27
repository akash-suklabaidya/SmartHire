package com.backend.smarthire.controller;
import com.backend.smarthire.dto.ApiResponse;
import com.backend.smarthire.model.User;
import com.backend.smarthire.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("NullableProblems")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // When someone posts to /api/auth/register, this method runs
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody User user) {
        User savedUSer=userService.registerUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful!", savedUSer));
    }
}