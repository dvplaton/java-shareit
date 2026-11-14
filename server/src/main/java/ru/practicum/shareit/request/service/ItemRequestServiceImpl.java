package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ItemRequest saved = itemRequestRepository.save(
                ItemRequestMapper.toItemRequest(dto, requestor));

        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<ItemRequest> requests =
                itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return mapWithAnswers(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<ItemRequest> requests =
                itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        return mapWithAnswers(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<Item> answers = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toItemRequestDto(request, answers);
    }

    private List<ItemRequestDto> mapWithAnswers(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> ids = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<Item>> answersByRequest = itemRepository.findByRequestIdIn(ids).stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));

        return requests.stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(
                        r, answersByRequest.getOrDefault(r.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}