package com.ctg.aiFab.gateway.controller;

import com.ctg.aiFab.gateway.dto.LoginRequest;
import com.ctg.aiFab.gateway.dto.LoginResponse;
import com.ctg.aiFab.gateway.entity.User;
import com.ctg.aiFab.gateway.service.JwtService;
import com.ctg.aiFab.gateway.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController集成测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setRealName("测试用户");
        user.setStatus(1);

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(1L, "testuser")).thenReturn("test-token");
        when(userService.getRoleCodes(1L)).thenReturn(List.of("user"));
        when(userService.getPermissionCodes(1L)).thenReturn(List.of("read"));

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("test-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }
}
