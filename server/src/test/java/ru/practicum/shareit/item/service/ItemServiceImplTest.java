package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    private Long ownerId;
    private Long bookerId;

    @BeforeEach
    void setUp() {
        ownerId = userService.create(user("Owner", "owner@test.com")).getId();
        bookerId = userService.create(user("Booker", "booker@test.com")).getId();
    }

    @Test
    void create_shouldSaveItem() {
        ItemDto created = itemService.create(ownerId, item("Дрель", "600Вт", true));

        assertThat(created.getId(), notNullValue());
        assertThat(created.getName(), equalTo("Дрель"));
        assertThat(created.getAvailable(), equalTo(true));
    }

    @Test
    void create_withRequestId_shouldLinkRequest() {
        Long requestId = itemRequestService.create(bookerId, new ItemRequestCreateDto("нужна дрель")).getId();

        ItemDto created = itemService.create(ownerId, ItemDto.builder()
                .name("Дрель")
                .description("ответ")
                .available(true)
                .requestId(requestId)
                .build());

        assertThat(created.getRequestId(), equalTo(requestId));
    }

    @Test
    void create_whenUserMissing_shouldThrow() {
        assertThrows(NotFoundException.class,
                () -> itemService.create(999L, item("A", "B", true)));
    }

    @Test
    void update_shouldChangeFields() {
        ItemDto created = itemService.create(ownerId, item("Old", "Desc", true));

        ItemDto patch = ItemDto.builder().name("New").available(false).build();
        ItemDto updated = itemService.update(ownerId, created.getId(), patch);

        assertThat(updated.getName(), equalTo("New"));
        assertThat(updated.getAvailable(), equalTo(false));
        assertThat(updated.getDescription(), equalTo("Desc"));
    }

    @Test
    void update_whenNotOwner_shouldThrow() {
        ItemDto created = itemService.create(ownerId, item("X", "Y", true));

        assertThrows(ForbiddenException.class,
                () -> itemService.update(bookerId, created.getId(), ItemDto.builder().name("Hacked").build()));
    }

    @Test
    void getById_shouldReturnCommentsForAnyUser() {
        ItemDto item = itemService.create(ownerId, item("Вещь", "описание", true));

        BookItemRequestDto book = new BookItemRequestDto();
        book.setItemId(item.getId());
        book.setStart(LocalDateTime.now().minusDays(2));
        book.setEnd(LocalDateTime.now().minusDays(1));
        var booking = bookingService.create(bookerId, book);
        bookingService.approve(ownerId, booking.getId(), true);

        itemService.addComment(bookerId, item.getId(), new NewCommentRequest("супер"));

        ItemDto found = itemService.getById(item.getId(), bookerId);

        assertThat(found.getComments(), hasSize(1));
        assertThat(found.getComments().get(0).getText(), equalTo("супер"));
    }

    @Test
    void getAllByOwner_shouldReturnOnlyOwnerItems() {
        itemService.create(ownerId, item("Дрель", "d1", true));
        itemService.create(ownerId, item("Пила", "d2", false));
        itemService.create(bookerId, item("Чужое", "x", true));

        List<ItemDto> items = itemService.getAllByOwner(ownerId);

        assertThat(items, hasSize(2));
        assertThat(items.stream().map(ItemDto::getName).toList(), containsInAnyOrder("Дрель", "Пила"));
    }

    @Test
    void search_shouldFindByNameOrDescription() {
        itemService.create(ownerId, item("ДрельСалют", "мощная", true));
        itemService.create(ownerId, item("Молоток", "обычный", true));
        itemService.create(ownerId, item("Скрыто", "дрель в тексте", false));

        List<ItemDto> found = itemService.search("дрель");

        assertThat(found.stream().map(ItemDto::getName).toList(), hasItem("ДрельСалют"));
        assertThat(found.stream().anyMatch(i -> !i.getAvailable()), equalTo(false));
    }

    @Test
    void search_whenBlank_shouldReturnEmpty() {
        assertThat(itemService.search("  "), empty());
        assertThat(itemService.search(null), empty());
    }

    @Test
    void addComment_withoutBooking_shouldThrow() {
        ItemDto item = itemService.create(ownerId, item("X", "Y", true));

        assertThrows(BadRequestException.class,
                () -> itemService.addComment(bookerId, item.getId(), new NewCommentRequest("нет")));
    }

    private static UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private static ItemDto item(String name, String description, boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}