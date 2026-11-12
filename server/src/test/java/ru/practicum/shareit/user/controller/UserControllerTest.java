package ru.practicum.shareit.user.controller;

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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturnUser() throws Exception {
        UserDto request = new UserDto(null, "Ivan", "ivan@mail.com");
        UserDto response = new UserDto(1L, "Ivan", "ivan@mail.com");
        when(userService.create(any())).thenReturn(response);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getById(99L)).thenThrow(new NotFoundException("not found"));

        mvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        when(userService.update(eq(1L), any())).thenReturn(new UserDto(1L, "N", "n@mail.com"));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"N\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("N"));
    }
}