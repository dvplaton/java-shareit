package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnItem() throws Exception {
        ItemDto request = ItemDto.builder()
                .name("Дрель")
                .description("600Вт")
                .available(true)
                .build();
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("600Вт")
                .available(true)
                .build();
        when(itemService.create(eq(1L), any())).thenReturn(response);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        when(itemService.getById(5L, 1L)).thenReturn(
                ItemDto.builder().id(5L).name("Пила").available(true).build());

        mvc.perform(get("/items/5").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void search_shouldReturnList() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Дрель").available(true).build()));

        mvc.perform(get("/items/search").param("text", "дрель").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        when(itemService.addComment(eq(2L), eq(1L), any()))
                .thenReturn(CommentDto.builder().id(1L).text("ок").authorName("Bob").build());

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new NewCommentRequest("ок"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("ок"));
    }
}