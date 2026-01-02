package com.modern.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modern.kitchensink.dto.MemberRequest;
import com.modern.kitchensink.dto.MemberResponse;
import com.modern.kitchensink.service.MemberService;
import com.modern.kitchensink.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllMembers_ShouldReturnListAnd200() throws Exception {
        MemberResponse member1 = new MemberResponse("1", "John Doe", "john@example.com", "9876543210", LocalDateTime.now(), LocalDateTime.now(), "user", "user");
        MemberResponse member2 = new MemberResponse("2", "Jane Doe", "jane@example.com", "9087654321", LocalDateTime.now(), LocalDateTime.now(), "user", "user");

        when(memberService.getAllMembers()).thenReturn(List.of(member1, member2));

        mockMvc.perform(get("/api/member")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void registerMember_WhenValidRequest_ShouldReturn201() throws Exception {
        MemberRequest request = new MemberRequest("John Doe", "john@example.com", "9876543210");
        MemberResponse response = new MemberResponse("1", "John Doe", "john@example.com", "9876543210", LocalDateTime.now(), LocalDateTime.now(), "user", "user");

        when(memberService.registerMember(any(MemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void registerMember_WhenInvalidRequest_ShouldReturn400() throws Exception {
        MemberRequest invalidRequest = new MemberRequest("", "invalid-email", "");

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void registerMember_WhenNameHasNumbers_ShouldReturn400() throws Exception {
        MemberRequest invalidRequest = new MemberRequest("John123", "john@test.com", "9876543210");

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must not contain numbers"));
    }

    @Test
    void registerMember_WhenNameTooLong_ShouldReturn400() throws Exception {
        String longName = "Christopher Alexander The Great King";
        MemberRequest invalidRequest = new MemberRequest(longName, "john@test.com", "9876543210");

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 1 and 25 characters"));
    }

    @Test
    void registerMember_WhenPhoneInvalidFormat_ShouldReturn400() throws Exception {
        MemberRequest invalidRequest = new MemberRequest("John Doe", "john@test.com", "1234567890");

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone number must be valid 10-digit Indian number"));
    }

    @Test
    void deleteMember_ShouldReturn204() throws Exception {
        doNothing().when(memberService).deleteMember(anyString());
        mockMvc.perform(delete("/api/member/123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void registerMember_WhenInternalError_ShouldReturn500() throws Exception {
        MemberRequest request = new MemberRequest("John", "john@test.com", "9876543210");

        when(memberService.registerMember(any(MemberRequest.class)))
                .thenThrow(new RuntimeException("Unexpected System Failure"));

        mockMvc.perform(post("/api/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}