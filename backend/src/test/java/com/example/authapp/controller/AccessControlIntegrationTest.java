package com.example.authapp.controller;

import com.example.authapp.entity.Role;
import com.example.authapp.entity.User;
import com.example.authapp.repository.UserRepository;
import com.example.authapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void initUsers() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPasswordHash(passwordEncoder.encode("Passw0rdA"));
        user.setRole(Role.USER);
        user = userRepository.save(user);

        User admin = new User();
        admin.setUsername("admin1");
        admin.setEmail("admin1@example.com");
        admin.setPasswordHash(passwordEncoder.encode("Passw0rdA"));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        userToken = jwtService.generateAccessToken(user.getId(), Role.USER);
        adminToken = jwtService.generateAccessToken(admin.getId(), Role.ADMIN);
    }

    @Test
    void meShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userShouldNotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
