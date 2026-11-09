package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create_shouldReturnRequest() throws Exception {
        ItemRequestDto response = ItemRequestDto.builder()
                .id(1L)
                .description("дрель")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
        when(itemRequestService.create(eq(1L), any())).thenReturn(response);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemRequestCreateDto("дрель"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("дрель"));
    }

    @Test
    void getOwn_shouldReturnList() throws Exception {
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(
                ItemRequestDto.builder().id(1L).description("a").items(List.of()).build(),
                ItemRequestDto.builder().id(2L).description("b").items(List.of()).build()));

        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(itemRequestService.getAll(1L)).thenReturn(List.of());

        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(itemRequestService.getById(1L, 99L))
                .thenThrow(new NotFoundException("not found"));

        mvc.perform(get("/requests/99").header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }
}