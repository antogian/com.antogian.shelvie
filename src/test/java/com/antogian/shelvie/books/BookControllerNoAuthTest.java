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
        BookRequest request = new BookRequest("Clean Code", "Robert C. Martin", "9780132350884", false);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/books/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void rejectsMissingTitle() throws Exception {
        BookRequest request = new BookRequest("", "Robert C. Martin", null, false);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingAuthor() throws Exception {
        BookRequest request = new BookRequest("Clean Code", "", null, false);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isbnIsOptional() throws Exception {
        BookRequest request = new BookRequest("Clean Code", "Robert C. Martin", null, false);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").doesNotExist());
    }

    @Test
    void markBookAsAlreadyRead() throws Exception {
        BookRequest request = new BookRequest("Clean Code", "Robert C. Martin", null, true);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.read").value(true));
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
        postBook("Clean Code", "Robert C. Martin", false);
        postBook("The Pragmatic Programmer", "David Thomas", true);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[1].title").value("The Pragmatic Programmer"));
    }

    @Test
    void filtersByReadTrue() throws Exception {
        postBook("Clean Code", "Robert C. Martin", false);
        postBook("The Pragmatic Programmer", "David Thomas", true);

        mockMvc.perform(get("/api/books").param("read", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Pragmatic Programmer"));
    }

    @Test
    void filtersByReadFalse() throws Exception {
        postBook("Clean Code", "Robert C. Martin", false);
        postBook("The Pragmatic Programmer", "David Thomas", true);

        mockMvc.perform(get("/api/books").param("read", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    // ── GET by ID ────────────────────────────────────────────────────────────

    @Test
    void returnsBookById() throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BookRequest("Clean Code", "Robert C. Martin", null, false))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void returns404ForMissingBook() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void postBook(String title, String author, boolean read) throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookRequest(title, author, null, read))));
    }
}