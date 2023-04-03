package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto getById(Long requestId, Long userId);

    List<ItemRequestDto> getAllByRequestorId(Long requestorId);

    List<ItemRequestDto> getAllByOtherUsers(Long userId, Pageable pageable);

    ItemRequestDto save(ItemRequestDto itemRequestDto);

}
