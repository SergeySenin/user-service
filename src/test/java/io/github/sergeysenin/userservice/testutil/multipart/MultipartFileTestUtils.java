package io.github.sergeysenin.userservice.testutil.multipart;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Утильные методы для создания тестовых объектов {@link MultipartFile}.
 */
public final class MultipartFileTestUtils {

    private MultipartFileTestUtils() {
    }

    // Возвращает билдера тестового MultipartFile с нейтральными значениями по умолчанию.
    public static MultipartFileTestBuilder multipartFile() {
        return new MultipartFileTestBuilder();
    }

    /**
     * Билдер тестовых реализаций {@link MultipartFile} с возможностью подменять отдельные свойства.
     */
    public static final class MultipartFileTestBuilder {

        private String name = "file";
        private String originalFilename = "test.bin";
        private String contentType = "application/octet-stream";
        private byte[] content = {0x01};
        private boolean empty;
        private long size = content.length;
        private IOException bytesException;

        MultipartFileTestBuilder() {
        }

        // Позволяет задать имя параметра формы.
        public MultipartFileTestBuilder withName(String value) {
            this.name = value;
            return this;
        }

        // Позволяет задать оригинальное имя файла.
        public MultipartFileTestBuilder withOriginalFilename(String value) {
            this.originalFilename = value;
            return this;
        }

        // Позволяет задать MIME-тип файла.
        public MultipartFileTestBuilder withContentType(String value) {
            this.contentType = value;
            return this;
        }

        // Позволяет задать содержимое файла.
        public MultipartFileTestBuilder withContent(byte[] value) {
            this.content = value == null ? null : value.clone();
            if (this.content != null && !this.empty) {
                this.size = this.content.length;
            }
            return this;
        }

        // Позволяет переопределить признак пустого файла.
        public MultipartFileTestBuilder withEmpty(boolean value) {
            this.empty = value;
            return this;
        }

        // Позволяет переопределить размер файла.
        public MultipartFileTestBuilder withSize(long value) {
            this.size = value;
            return this;
        }

        // Позволяет настроить исключение, выбрасываемое при чтении содержимого.
        public MultipartFileTestBuilder withBytesException(IOException value) {
            this.bytesException = value;
            return this;
        }

        // Создаёт тестовую реализацию MultipartFile с учётом всех настроек билдера.
        public MultipartFile build() {
            byte[] safeContent = content == null ? null : Arrays.copyOf(content, content.length);
            return new TestMultipartFile(name, originalFilename, contentType, safeContent, empty, size, bytesException);
        }
    }

    private record TestMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            byte[] content,
            boolean empty,
            long size,
            IOException bytesException
    ) implements MultipartFile {

        @Override
        public @NonNull String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return empty;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        @NonNull
        public byte [] getBytes() throws IOException {
            if (bytesException != null) {
                throw bytesException;
            }
            return content == null ? new byte[0] : Arrays.copyOf(content, content.length);
        }

        @Override
        public @NonNull InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(@NonNull File dest) throws IOException {
            throw new IOException("transferTo не поддерживается в тестовой реализации");
        }
    }
}
