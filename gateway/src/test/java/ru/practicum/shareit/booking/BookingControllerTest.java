package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create_shouldReturnOk() throws Exception {
        when(bookingClient.create(eq(2L), any())).thenReturn(
                ResponseEntity.ok(Map.of("id", 5, "status", "WAITING")));

        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void approve_shouldReturnOk() throws Exception {
        when(bookingClient.approve(1L, 5L, true)).thenReturn(
                ResponseEntity.ok(Map.of("id", 5, "status", "APPROVED")));

        mvc.perform(patch("/bookings/5")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(bookingClient.getById(2L, 5L)).thenReturn(
                ResponseEntity.ok(Map.of("id", 5)));

        mvc.perform(get("/bookings/5").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getAllByBooker_shouldReturnOk() throws Exception {
        when(bookingClient.getAllByBooker(2L, "ALL")).thenReturn(
                ResponseEntity.ok(List.of(Map.of("id", 5))));

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void getAllByOwner_shouldReturnOk() throws Exception {
        when(bookingClient.getAllByOwner(1L, "ALL")).thenReturn(
                ResponseEntity.ok(List.of(Map.of("id", 5))));

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }
}
