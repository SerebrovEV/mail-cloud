package org.image.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.image.core.dto.RegisterReq;
import org.image.core.dto.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DataSource dataSource;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterReq registerReq = new RegisterReq();
        registerReq.setEmail("test@example.com");
        registerReq.setPassword("Password123!");
        registerReq.setRole(Role.USER);

        String jsonRequest = objectMapper.writeValueAsString(registerReq);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    void testRegister_IncorrectEmail() throws Exception {
        RegisterReq registerReq = new RegisterReq();
        registerReq.setEmail("test@-example.com");
        registerReq.setPassword("Password123!");
        registerReq.setRole(Role.USER);

        String jsonRequest = objectMapper.writeValueAsString(registerReq);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testRegister_IncorrectPassword() throws Exception {
        RegisterReq registerReq = new RegisterReq();
        registerReq.setEmail("test@example.com");
        registerReq.setPassword("password123!");
        registerReq.setRole(Role.USER);

        String jsonRequest = objectMapper.writeValueAsString(registerReq);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_SuccessResponse() throws Exception {
        String email = "test@example.com";
        String password = "Password1234!";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO app_user (id, account_non_locked, email, password, role)
                    VALUES (1, true, 'test@example.com', '$2a$12$pCwgPzi0vLHBMzeeXxJ9SO1CXhNiQOu4NE.RbvZ3wjZHwiaOvhyJ.', 1)
                   """);
        }
        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin_ForbiddenResponse() throws Exception {
        String email = "test@example.com";
        String password = "wrongPassword";

        mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }
}