package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!existing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        BookingShortDto last = null;
        BookingShortDto next = null;

        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            last = bookingRepository
                    .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                            itemId, BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
            next = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            itemId, BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
        }

        return ItemMapper.toItemDto(item, last, next, comments);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();

        // все APPROVED-бронирования владельца по его вещам
        List<Booking> approved = bookingRepository
                .findByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.APPROVED);

        Map<Long, Booking> lastByItem = approved.stream()
                .filter(b -> b.getStart().isBefore(now))
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (a, b) -> a.getStart().isAfter(b.getStart()) ? a : b
                ));

        Map<Long, Booking> nextByItem = approved.stream()
                .filter(b -> b.getStart().isAfter(now))
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (a, b) -> a.getStart().isBefore(b.getStart()) ? a : b
                ));

        Map<Long, List<CommentDto>> commentsByItem = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        BookingMapper.toBookingShortDto(lastByItem.get(item.getId())),
                        BookingMapper.toBookingShortDto(nextByItem.get(item.getId())),
                        commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean canComment = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!canComment) {
            throw new BadRequestException(
                    "Оставлять комментарий может только пользователь, бравший вещь в аренду");
        }

        Comment comment = Comment.builder()
                .text(request.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}