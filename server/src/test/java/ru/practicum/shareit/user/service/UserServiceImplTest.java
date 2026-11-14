package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldReturnUserWithId() {
        UserDto created = userService.create(user("Ivan", "ivan@mail.com"));

        assertThat(created.getId(), notNullValue());
        assertThat(created.getName(), equalTo("Ivan"));
        assertThat(created.getEmail(), equalTo("ivan@mail.com"));
    }

    @Test
    void create_whenEmailExists_shouldThrow() {
        userService.create(user("A", "same@mail.com"));

        assertThrows(ConflictException.class,
                () -> userService.create(user("B", "same@mail.com")));
    }

    @Test
    void getById_shouldReturnUser() {
        UserDto created = userService.create(user("A", "a@mail.com"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getId(), equalTo(created.getId()));
        assertThat(found.getEmail(), equalTo("a@mail.com"));
    }

    @Test
    void getById_whenMissing_shouldThrow() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void update_shouldChangeNameAndEmail() {
        UserDto created = userService.create(user("Old", "old@mail.com"));

        UserDto patch = new UserDto();
        patch.setName("New");
        patch.setEmail("new@mail.com");

        UserDto updated = userService.update(created.getId(), patch);

        assertThat(updated.getName(), equalTo("New"));
        assertThat(updated.getEmail(), equalTo("new@mail.com"));
    }

    @Test
    void update_onlyName() {
        UserDto created = userService.create(user("Old", "keep@mail.com"));

        UserDto patch = new UserDto();
        patch.setName("OnlyName");

        UserDto updated = userService.update(created.getId(), patch);

        assertThat(updated.getName(), equalTo("OnlyName"));
        assertThat(updated.getEmail(), equalTo("keep@mail.com"));
    }

    @Test
    void getAll_shouldReturnCreatedUsers() {
        userService.create(user("U1", "u1@mail.com"));
        userService.create(user("U2", "u2@mail.com"));

        assertThat(userService.getAll().size(), greaterThanOrEqualTo(2));
    }

    @Test
    void delete_shouldRemoveUser() {
        UserDto created = userService.create(user("Del", "del@mail.com"));

        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }

    private static UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}