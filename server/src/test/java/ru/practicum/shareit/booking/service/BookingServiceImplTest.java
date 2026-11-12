package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        ownerId = userService.create(user("Owner", "o@test.com")).getId();
        bookerId = userService.create(user("Booker", "b@test.com")).getId();
        itemId = itemService.create(ownerId, ItemDto.builder()
                .name("Вещь").description("описание").available(true).build()).getId();
    }

    @Test
    void create_shouldBeWaiting() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));

        assertThat(created.getId(), notNullValue());
        assertThat(created.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void create_whenOwnerBooksOwnItem_shouldThrow() {
        assertThrows(NotFoundException.class,
                () -> bookingService.create(ownerId, book(itemId, 1, 2)));
    }

    @Test
    void create_whenItemNotAvailable_shouldThrow() {
        Long unavailable = itemService.create(ownerId, ItemDto.builder()
                .name("No").description("x").available(false).build()).getId();

        assertThrows(BadRequestException.class,
                () -> bookingService.create(bookerId, book(unavailable, 1, 2)));
    }

    @Test
    void create_whenEndBeforeStart_shouldThrow() {
        BookItemRequestDto req = new BookItemRequestDto();
        req.setItemId(itemId);
        req.setStart(LocalDateTime.now().plusDays(3));
        req.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> bookingService.create(bookerId, req));
    }

    @Test
    void approve_shouldSetApproved() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));

        BookingDto approved = bookingService.approve(ownerId, created.getId(), true);

        assertThat(approved.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approve_reject_shouldSetRejected() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));

        BookingDto rejected = bookingService.approve(ownerId, created.getId(), false);

        assertThat(rejected.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void approve_whenNotOwner_shouldThrow() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));

        assertThrows(ForbiddenException.class,
                () -> bookingService.approve(bookerId, created.getId(), true));
    }

    @Test
    void approve_whenAlreadyApproved_shouldThrow() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));
        bookingService.approve(ownerId, created.getId(), true);

        assertThrows(BadRequestException.class,
                () -> bookingService.approve(ownerId, created.getId(), true));
    }

    @Test
    void getById_forBookerAndOwner_shouldWork() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));

        assertThat(bookingService.getById(bookerId, created.getId()).getId(), equalTo(created.getId()));
        assertThat(bookingService.getById(ownerId, created.getId()).getId(), equalTo(created.getId()));
    }

    @Test
    void getById_forStranger_shouldThrow() {
        BookingDto created = bookingService.create(bookerId, book(itemId, 1, 2));
        Long stranger = userService.create(user("S", "s@test.com")).getId();

        assertThrows(NotFoundException.class,
                () -> bookingService.getById(stranger, created.getId()));
    }

    @Test
    void getAllByBooker_all_shouldReturnList() {
        bookingService.create(bookerId, book(itemId, 1, 2));

        List<BookingDto> list = bookingService.getAllByBooker(bookerId, BookingState.ALL);

        assertThat(list, hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void getAllByOwner_waiting_shouldReturn() {
        bookingService.create(bookerId, book(itemId, 1, 2));

        List<BookingDto> list = bookingService.getAllByOwner(ownerId, BookingState.WAITING);

        assertThat(list, hasSize(greaterThanOrEqualTo(1)));
        assertThat(list.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    private BookItemRequestDto book(Long itemId, int plusStartDays, int plusEndDays) {
        BookItemRequestDto req = new BookItemRequestDto();
        req.setItemId(itemId);
        req.setStart(LocalDateTime.now().plusDays(plusStartDays));
        req.setEnd(LocalDateTime.now().plusDays(plusEndDays));
        return req;
    }

    private static UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}