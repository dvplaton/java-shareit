package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private Long requestorId;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        requestorId = userService.create(user("Req", "req@test.com")).getId();
        ownerId = userService.create(user("Own", "own@test.com")).getId();
    }

    @Test
    void createAndGetOwn_shouldReturnRequestWithAnswerItems() {
        ItemRequestDto created = itemRequestService.create(
                requestorId, new ItemRequestCreateDto("нужна дрель"));

        assertThat(created.getId(), notNullValue());
        assertThat(created.getDescription(), equalTo("нужна дрель"));
        assertThat(created.getItems(), empty());

        itemService.create(ownerId, ItemDto.builder()
                .name("Дрель")
                .description("600Вт")
                .available(true)
                .requestId(created.getId())
                .build());

        List<ItemRequestDto> own = itemRequestService.getOwn(requestorId);

        assertThat(own, hasSize(1));
        assertThat(own.get(0).getItems(), hasSize(1));
        assertThat(own.get(0).getItems().get(0).getName(), equalTo("Дрель"));
        assertThat(own.get(0).getItems().get(0).getOwnerId(), equalTo(ownerId));
    }

    @Test
    void getAll_shouldNotReturnOwnRequests() {
        itemRequestService.create(requestorId, new ItemRequestCreateDto("свой"));
        ItemRequestDto foreign = itemRequestService.create(ownerId, new ItemRequestCreateDto("чужой"));

        List<ItemRequestDto> all = itemRequestService.getAll(requestorId);

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getId(), equalTo(foreign.getId()));
    }

    private static UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}