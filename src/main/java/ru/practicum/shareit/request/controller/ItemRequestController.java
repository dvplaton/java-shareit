package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_HEADER) Long userId,
                                 @Valid @RequestBody ItemRequestCreateDto dto) {
        return itemRequestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
}