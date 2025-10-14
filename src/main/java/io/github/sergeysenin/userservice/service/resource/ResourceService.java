package io.github.sergeysenin.userservice.service.resource;

/**
 * Контракт для работы с бинарными ресурсами (изображениями).
 */
public interface ResourceService {

    /**
     * Выполняет ресайз изображения, сохраняя пропорции и ограничивая максимальную сторону.
     *
     * @param originalBytes исходный массив байт изображения
     * @param maxSide       требуемая максимальная сторона (ширина или высота)
     * @param format        формат выходного изображения (jpg/png/webp и т.д.)
     * @return массив байт с изменённым размером изображения
     */
    byte[] resize(byte[] originalBytes, int maxSide, String format);
}
