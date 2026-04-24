package com.antogian.shelvie.books;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = "shelvie.security.mode=none")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookControllerNoAuthTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    @Test
    void createsBookAndReturns201() throws Exception {
        BookRequest request = new BookRequest(
                "Clean Code", "Robert C. Martin", "9780132350884",
                BookStatus.TO_READ, Set.of("Programming", "Software Engineering"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/books/")))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id", matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.status").value("To-Read"))
                .andExpect(jsonPath("$.genres", hasSize(2)));
    }

    @Test
    void acceptsStatusCaseInsensitively() throws Exception {
        String body = """
                {
                  "title": "Clean Code",
                  "author": "Robert C. Martin",
                  "status": "currently-reading",
                  "genres": ["Programming"]
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Currently-Reading"));
    }

    @Test
    void rejectsMissingStatus() throws Exception {
        String body = """
                {
                  "title": "Clean Code",
                  "author": "Robert C. Martin",
                  "genres": ["Programming"]
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyGenres() throws Exception {
        String body = """
                {
                  "title": "Clean Code",
                  "author": "Robert C. Martin",
                  "status": "To-Read",
                  "genres": []
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingGenres() throws Exception {
        String body = """
                {
                  "title": "Clean Code",
                  "author": "Robert C. Martin",
                  "status": "To-Read"
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingTitle() throws Exception {
        BookRequest request = new BookRequest(
                "", "Robert C. Martin", null,
                BookStatus.TO_READ, Set.of("Programming"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingAuthor() throws Exception {
        BookRequest request = new BookRequest(
                "Clean Code", "", null,
                BookStatus.TO_READ, Set.of("Programming"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isbnIsOptional() throws Exception {
        BookRequest request = new BookRequest(
                "Clean Code", "Robert C. Martin", null,
                BookStatus.TO_READ, Set.of("Programming"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").doesNotExist());
    }

    // ── GET all ──────────────────────────────────────────────────────────────

    @Test
    void returnsEmptyListWhenNoBooksExist() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void returnsAllBooks() throws Exception {
        postBook("Clean Code", "Robert C. Martin", BookStatus.TO_READ);
        postBook("The Pragmatic Programmer", "David Thomas", BookStatus.READ);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void filtersByStatus() throws Exception {
        postBook("Clean Code", "Robert C. Martin", BookStatus.TO_READ);
        postBook("The Pragmatic Programmer", "David Thomas", BookStatus.READ);
        postBook("Refactoring", "Martin Fowler", BookStatus.CURRENTLY_READING);

        mockMvc.perform(get("/api/books").param("status", "Read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Pragmatic Programmer"));

        mockMvc.perform(get("/api/books").param("status", "To-Read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        mockMvc.perform(get("/api/books").param("status", "Currently-Reading"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Refactoring"));
    }

    // ── GET by ID ────────────────────────────────────────────────────────────

    @Test
    void returnsBookById() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.status").value("To-Read"));
    }

    @Test
    void returns404ForMissingBook() throws Exception {
        mockMvc.perform(get("/api/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── PATCH ────────────────────────────────────────────────────────────────

    @Test
    void updatesBookAndReturns200() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        String body = """
            {
              "title": "Clean Code (2nd Ed.)",
              "author": "Robert C. Martin",
              "status": "Read",
              "genres": ["Programming"]
            }
            """;

        mockMvc.perform(patch("/api/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Clean Code (2nd Ed.)"))
                .andExpect(jsonPath("$.status").value("Read"));
    }

    @Test
    void updateClearsIsbnWhenOmitted() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        String body = """
            {
              "title": "Clean Code",
              "author": "Robert C. Martin",
              "status": "Read",
              "genres": ["Programming"]
            }
            """;

        mockMvc.perform(patch("/api/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").doesNotExist());
    }

    @Test
    void updatePreservesIsbnWhenProvided() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        String body = """
            {
              "title": "Clean Code",
              "author": "Robert C. Martin",
              "isbn": "9780132350884",
              "status": "Read",
              "genres": ["Programming"]
            }
            """;

        mockMvc.perform(patch("/api/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    void returns404WhenUpdatingNonExistentBook() throws Exception {
        String body = """
            {
              "title": "Clean Code",
              "author": "Robert C. Martin",
              "status": "To-Read",
              "genres": ["Programming"]
            }
            """;

        mockMvc.perform(patch("/api/books/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRejectsMissingTitle() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        String body = """
            {
              "title": "",
              "author": "Robert C. Martin",
              "status": "Read",
              "genres": ["Programming"]
            }
            """;

        mockMvc.perform(patch("/api/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deletesBookAndReturns204() throws Exception {
        String id = postBookAndGetId("Clean Code", "Robert C. Martin", BookStatus.TO_READ);

        mockMvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns404WhenDeletingNonExistentBook() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void postBook(String title, String author, BookStatus status) throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookRequest(title, author, null, status, Set.of("Fiction")))));
    }

    private String postBookAndGetId(String title, String author, BookStatus status) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BookRequest(title, author, null, status, Set.of("Fiction")))))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}