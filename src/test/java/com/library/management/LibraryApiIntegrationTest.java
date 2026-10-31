package com.library.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.book.model.BookDocument;
import com.library.management.book.repository.BookRepository;
import com.library.management.support.MongoTestcontainersConfig;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import com.library.management.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class LibraryApiIntegrationTest extends MongoTestcontainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String bookId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        bookRepository.deleteAll();

        UserDocument admin = new UserDocument();
        admin.setEmail("admin@library.local");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        admin.setFullName("Library Admin");
        admin.setRoles(Set.of(Role.ADMIN));
        admin.setEnabled(true);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        userRepository.save(admin);

        UserDocument member = new UserDocument();
        member.setEmail("member@library.local");
        member.setPasswordHash(passwordEncoder.encode("MemberPass1"));
        member.setFullName("Library Member");
        member.setRoles(Set.of(Role.MEMBER));
        member.setEnabled(true);
        member.setCreatedAt(Instant.now());
        member.setUpdatedAt(Instant.now());
        userRepository.save(member);

        BookDocument book = new BookDocument();
        book.setTitle("Seed Book");
        book.setAuthorName("Seed Author");
        book.setIsbn("9780134685991");
        book.setDescription("A seeded book");
        book.setPublicationYear(2018);
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());
        bookId = bookRepository.save(book).getId();
    }

    @Test
    void registerLoginAndUpdateBookHappyPath() throws Exception {
        String email = "intern+" + UUID.randomUUID() + "@library.local";

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "InternPass1",
                                  "fullName": "New Intern"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));

        String adminToken = login("admin@library.local", "ChangeMe123!");

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Seed Book",
                                  "authorName": "Updated Author",
                                  "isbn": "9780596009205",
                                  "description": "Updated by integration test",
                                  "publicationYear": 2021,
                                  "totalCopies": 6,
                                  "availableCopies": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Seed Book"))
                .andExpect(jsonPath("$.isbn").value("9780596009205"))
                .andExpect(jsonPath("$.availableCopies").value(5));

        assertThat(bookRepository.findById(bookId)).isPresent();
        assertThat(bookRepository.findById(bookId).orElseThrow().getTitle()).isEqualTo("Updated Seed Book");
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "fullName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void updateBookRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdatePayload()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateBookForbidsMemberRole() throws Exception {
        String memberToken = login("member@library.local", "MemberPass1");

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdatePayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@library.local",
                                  "password": "WrongPassword1"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String validUpdatePayload() {
        return """
                {
                  "title": "Updated Seed Book",
                  "authorName": "Updated Author",
                  "isbn": "9780596009205",
                  "description": "Updated by integration test",
                  "publicationYear": 2021,
                  "totalCopies": 6,
                  "availableCopies": 5
                }
                """;
    }
}
