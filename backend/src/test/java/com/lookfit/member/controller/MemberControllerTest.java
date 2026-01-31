package com.lookfit.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lookfit.member.domain.Member;
import com.lookfit.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll(); // Clean up before each test
    }

    @Test
    void testCreateAndGetMember() throws Exception {
        // Create a new member
        Member newMember = new Member();
        newMember.setMemberid("test_member_id_1");
        newMember.setMembername("Test User");
        newMember.setEmail("test@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberid").value("test_member_id_1"))
                .andExpect(jsonPath("$.membername").value("Test User"));

        mockMvc.perform(get("/api/members/{id}", newMember.getMemberid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membername").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
