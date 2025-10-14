package io.github.sergeysenin.userservice.service.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
ResourceService / ImageResourceService — контракт и реализация ресайза изображений с помощью Thumbnailator.

Использует Thumbnailator; предоставляется AvatarService через интерфейс ResourceService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResourceService implements ResourceService {

    // Реализованный метод через @Override
}
