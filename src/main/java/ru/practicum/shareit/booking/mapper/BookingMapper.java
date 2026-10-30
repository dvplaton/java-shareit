package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .build();
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static Booking toBooking(BookItemRequestDto request, Item item, User booker) {
        return Booking.builder()
                .start(request.getStart())
                .end(request.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }
}