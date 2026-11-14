package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnBooking() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto response = BookingDto.builder()
                .id(10L)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingService.create(eq(2L), any())).thenReturn(response);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_shouldReturnApproved() throws Exception {
        when(bookingService.approve(1L, 10L, true))
                .thenReturn(BookingDto.builder().id(10L).status(BookingStatus.APPROVED).build());

        mvc.perform(patch("/bookings/10")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        when(bookingService.getById(2L, 10L))
                .thenReturn(BookingDto.builder().id(10L).status(BookingStatus.WAITING).build());

        mvc.perform(get("/bookings/10").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getAllByBooker_shouldReturnList() throws Exception {
        when(bookingService.getAllByBooker(eq(2L), any()))
                .thenReturn(List.of(BookingDto.builder().id(1L).build()));

        mvc.perform(get("/bookings").param("state", "ALL").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllByOwner_shouldReturnOk() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), any()))
                .thenReturn(List.of(BookingDto.builder().id(3L).status(BookingStatus.WAITING).build()));

        mvc.perform(get("/bookings/owner").param("state", "ALL").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }
}