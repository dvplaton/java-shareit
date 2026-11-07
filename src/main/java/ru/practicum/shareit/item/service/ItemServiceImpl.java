package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        Map<Long, ItemDto> enriched = enrichItems(List.of(item), userId);
        return enriched.get(item.getId());
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        Map<Long, ItemDto> enriched = enrichItems(items, userId);
        return items.stream()
                .map(item -> enriched.get(item.getId()))
                .collect(Collectors.toList());
    }

    private Map<Long, ItemDto> enrichItems(List<Item> items, Long userId) {
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<CommentDto>> commentsByItem = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        Map<Long, Booking> lastByItem = Map.of();
        Map<Long, Booking> nextByItem = Map.of();

        boolean isOwnerView = !items.isEmpty()
                && userId != null
                && items.get(0).getOwner().getId().equals(userId);

        // для getById список из 1 item — owner проверяется по этой вещи;
        // для getAllByOwner все вещи одного владельца
        if (userId != null) {
            List<Item> ownerItems = items.stream()
                    .filter(i -> i.getOwner().getId().equals(userId))
                    .collect(Collectors.toList());

            if (!ownerItems.isEmpty()) {
                List<Long> ownerItemIds = ownerItems.stream().map(Item::getId).collect(Collectors.toList());
                List<Booking> approved = bookingRepository
                        .findByItemIdInAndStatusOrderByStartDesc(ownerItemIds, BookingStatus.APPROVED);

                lastByItem = approved.stream()
                        .filter(b -> b.getStart().isBefore(now))
                        .collect(Collectors.toMap(
                                b -> b.getItem().getId(),
                                b -> b,
                                (a, b) -> a.getStart().isAfter(b.getStart()) ? a : b
                        ));

                nextByItem = approved.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toMap(
                                b -> b.getItem().getId(),
                                b -> b,
                                (a, b) -> a.getStart().isBefore(b.getStart()) ? a : b
                        ));
            }
        }

        Map<Long, Booking> lastFinal = lastByItem;
        Map<Long, Booking> nextFinal = nextByItem;

        return items.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        item -> {
                            boolean showBookings = userId != null
                                    && item.getOwner().getId().equals(userId);
                            return ItemMapper.toItemDto(
                                    item,
                                    showBookings
                                            ? BookingMapper.toBookingShortDto(lastFinal.get(item.getId()))
                                            : null,
                                    showBookings
                                            ? BookingMapper.toBookingShortDto(nextFinal.get(item.getId()))
                                            : null,
                                    commentsByItem.getOrDefault(item.getId(), List.of())
                            );
                        }
                ));
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

        Comment comment = CommentMapper.toComment(request, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}