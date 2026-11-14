package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_shouldReturnOk() throws Exception {
        when(itemClient.create(eq(1L), any())).thenReturn(
                ResponseEntity.ok(Map.of("id", 10, "name", "Дрель")));

        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("600Вт");
        dto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        when(itemClient.update(eq(1L), eq(10L), any())).thenReturn(
                ResponseEntity.ok(Map.of("id", 10, "name", "New")));

        mvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(itemClient.getById(1L, 10L)).thenReturn(
                ResponseEntity.ok(Map.of("id", 10, "name", "Дрель")));

        mvc.perform(get("/items/10").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getAllByOwner_shouldReturnOk() throws Exception {
        when(itemClient.getAllByOwner(1L)).thenReturn(
                ResponseEntity.ok(List.of(Map.of("id", 10))));

        mvc.perform(get("/items").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void search_shouldReturnOk() throws Exception {
        when(itemClient.search(1L, "дрель")).thenReturn(
                ResponseEntity.ok(List.of(Map.of("name", "Дрель"))));

        mvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_shouldReturnOk() throws Exception {
        when(itemClient.addComment(eq(2L), eq(10L), any())).thenReturn(
                ResponseEntity.ok(Map.of("id", 1, "text", "ok")));

        mvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new NewCommentRequest("ok"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("ok"));
    }
}
