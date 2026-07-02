package com.ctg.aiFab.gateway.controller;

import com.ctg.aiFab.gateway.dto.LoginRequest;
import com.ctg.aiFab.gateway.dto.MfaVerifyRequest;
import com.ctg.aiFab.gateway.entity.User;
import com.ctg.aiFab.gateway.service.JwtService;
import com.ctg.aiFab.gateway.service.MfaService;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController集成测试 - C2双因子认证
 *
 * @author Developer
 * @since 2026-07-02
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

    @MockBean
    private MfaService mfaService;

    @Test
    void testLogin_WithoutMfa_Success() throws Exception {
        // User without MFA enabled
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setRealName("测试用户");
        user.setStatus(1);
        user.setMfaEnabled(false);

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
    void testLogin_WithMfa_ReturnsTempToken() throws Exception {
        // User with MFA enabled
        User user = new User();
        user.setId(2L);
        user.setUsername("mfauser");
        user.setPassword("encoded-password");
        user.setRealName("MFA用户");
        user.setStatus(1);
        user.setMfaEnabled(true);

        when(userService.findByUsername("mfauser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setUsername("mfauser");
        request.setPassword("password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mfaRequired").value(true))
                .andExpect(jsonPath("$.data.tempToken").exists())
                .andExpect(jsonPath("$.data.token").doesNotExist());
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

    @Test
    void testMfaVerify_Success() throws Exception {
        // First, get temp token from login
        User user = new User();
        user.setId(2L);
        user.setUsername("mfauser");
        user.setPassword("encoded-password");
        user.setRealName("MFA用户");
        user.setStatus(1);
        user.setMfaEnabled(true);
        user.setMfaSecret("TOTP_SECRET_KEY");

        when(userService.findByUsername("mfauser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(userService.findById(2L)).thenReturn(user);
        when(mfaService.verifyCode(eq("TOTP_SECRET_KEY"), eq("123456"))).thenReturn(true);
        when(jwtService.generateToken(2L, "mfauser")).thenReturn("jwt-token");
        when(userService.getRoleCodes(2L)).thenReturn(List.of("user"));
        when(userService.getPermissionCodes(2L)).thenReturn(List.of("read"));

        // Step 1: Login to get temp token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("mfauser");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mfaRequired").value(true))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String tempToken = objectMapper.readTree(responseBody).get("data").get("tempToken").asText();

        // Step 2: Verify MFA code
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setTempToken(tempToken);
        mfaRequest.setCode("123456");

        mockMvc.perform(post("/auth/mfa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("mfauser"));
    }

    @Test
    void testMfaVerify_InvalidCode() throws Exception {
        // First, get temp token from login
        User user = new User();
        user.setId(2L);
        user.setUsername("mfauser");
        user.setPassword("encoded-password");
        user.setRealName("MFA用户");
        user.setStatus(1);
        user.setMfaEnabled(true);
        user.setMfaSecret("TOTP_SECRET_KEY");

        when(userService.findByUsername("mfauser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(userService.findById(2L)).thenReturn(user);
        when(mfaService.verifyCode(eq("TOTP_SECRET_KEY"), eq("999999"))).thenReturn(false);

        // Step 1: Login to get temp token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("mfauser");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String tempToken = objectMapper.readTree(responseBody).get("data").get("tempToken").asText();

        // Step 2: Verify with wrong MFA code
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setTempToken(tempToken);
        mfaRequest.setCode("999999");

        mockMvc.perform(post("/auth/mfa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }
}
