package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.validation.Create;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Название не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Название не может состоять из пробелов")
    private String name;

    @NotBlank(groups = Create.class, message = "Описание не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Описание не может состоять из пробелов")
    private String description;

    @NotNull(groups = Create.class, message = "Статус доступности должен быть указан")
    private Boolean available;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
}