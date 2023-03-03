package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper requestMapper;
    private final ItemMapper itemMapper;


    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        getUser(userId);
        ItemRequest request = getItemRequest(requestId);
        return makeDto(request);
    }

    @Override
    public List<ItemRequestDto> getAllByRequestorId(Long requestorId) {
        getUser(requestorId);
        List<ItemRequest> requests = requestRepository.getAllByRequestorIdOrderByCreatedDesc(requestorId);
        return makeDtos(requests);
    }

    @Override
    public List<ItemRequestDto> getAllByOtherUsers(Long userId, Pageable pageable) {
        getUser(userId);
        List<ItemRequest> requests = requestRepository.findAllByOtherUsers(userId, pageable);
        return makeDtos(requests);
    }

    @Override
    @Transactional
    public ItemRequestDto save(ItemRequestDto requestDto) {
        User requestor = getUser(requestDto.getRequestorId());
        ItemRequest request = requestMapper.toItemRequest(requestDto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        return requestMapper.toItemRequestDto(requestRepository.save(request));
    }

    // -------------------------
    // Вспомогательные методы
    // -------------------------

    private ItemRequest getItemRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден запрос с ID " + requestId, ItemRequest.class
                ));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Не найден пользователь с ID " + userId, User.class
                ));
    }

    private List<ItemRequestDto> makeDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) return Collections.emptyList();

        List<ItemRequestDto> requestDtos = requests.stream()
                .map(requestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        addItems(requestDtos);

        return requestDtos;
    }

    private ItemRequestDto makeDto(ItemRequest request) {
        ItemRequestDto requestDto = requestMapper.toItemRequestDto(request);
        addItems(requestDto);
        return requestDto;
    }

    private void addItems(ItemRequestDto requestDto) {
        List<Item> items = itemRepository.findAllByRequestId(requestDto.getId());
        if (items.isEmpty()) return;
        items.forEach(item -> requestDto.getItems()
                .add(itemMapper.toItemItemRequestDto(item)));
    }

    private void addItems(List<ItemRequestDto> requestDtos) {
        if (requestDtos.isEmpty()) return;

        List<Long> requestIds = requestDtos.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());
        Map<Long, List<ItemItemRequestDto>> requestItems = new HashMap<>();

        List<Item> items = itemRepository.findAllByRequestIds(requestIds);
        if (items.isEmpty()) return;

        items.forEach(item -> requestItems
                .computeIfAbsent(item.getRequest().getId(), itemList -> new ArrayList<>())
                .add(itemMapper.toItemItemRequestDto(item)));

        requestDtos.forEach(requestDto -> requestDto.setItems(
                requestItems.getOrDefault(requestDto.getId(), Collections.emptyList())));
    }
}
