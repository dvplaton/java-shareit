package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = userService.create(user("Owner", "owner@test.com")).getId();
    }

    @Test
    void getAllByOwner_shouldReturnOwnerItems() {
        itemService.create(ownerId, ItemDto.builder()
                .name("Дрель").description("d1").available(true).build());
        itemService.create(ownerId, ItemDto.builder()
                .name("Пила").description("d2").available(false).build());

        Long other = userService.create(user("Other", "other@test.com")).getId();
        itemService.create(other, ItemDto.builder()
                .name("Чужое").description("x").available(true).build());

        List<ItemDto> items = itemService.getAllByOwner(ownerId);

        assertThat(items, hasSize(2));
        assertThat(items.stream().map(ItemDto::getName).toList(),
                containsInAnyOrder("Дрель", "Пила"));
    }

    private static UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}