package io.github.sergeysenin.userservice.service.resource;

public interface ResourceService {

    byte[] resize(byte[] originalBytes, int maxSide, String format);
}
