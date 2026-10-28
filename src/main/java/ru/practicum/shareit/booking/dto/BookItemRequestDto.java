package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

    @NotNull(message = "itemId обязателен")
    private Long itemId;

    @NotNull(message = "start обязателен")
    @FutureOrPresent(message = "start не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "end обязателен")
    @Future(message = "end должен быть в будущем")
    private LocalDateTime end;
}