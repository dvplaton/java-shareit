package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.practicum.shareit.validation.Create;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Имя не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Имя не может состоять из пробелов")
    private String name;
    @NotBlank(groups = Create.class, message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;
}