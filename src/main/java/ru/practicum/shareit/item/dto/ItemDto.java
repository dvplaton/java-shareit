package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.practicum.shareit.validation.Create;

@Data
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
}