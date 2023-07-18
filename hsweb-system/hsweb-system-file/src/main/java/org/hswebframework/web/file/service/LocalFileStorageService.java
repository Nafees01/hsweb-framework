package org.hswebframework.web.file.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.file.FileUploadProperties;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@AllArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final FileUploadProperties properties;

    @Override
    public Mono<String> saveFile(FilePart filePart) {
        FileUploadProperties.StaticFileInfo info = properties.createStaticSavePath(filePart.filename());
        return createStaticFileInfo(filePart.filename())
                .flatMap(into -> {
                    File file = new File(info.getSavePath());
                    return (filePart)
                            .transferTo(file)
                            .then(Mono.fromRunnable(() -> properties.applyFilePermission(file)))
                            .thenReturn(info.getLocation());
                });
    }

    private static final OpenOption[] FILE_CHANNEL_OPTIONS = {
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE};

    protected Mono<FileUploadProperties.StaticFileInfo> createStaticFileInfo(String fileName) {
        return Mono.just(properties.createStaticSavePath(fileName));
    }

    @Override
    @SneakyThrows
    public Mono<String> saveFile(InputStream inputStream, String fileType) {
        String fileName = "_temp" + (fileType.startsWith(".") ? fileType : "." + fileType);

        return createStaticFileInfo(fileName)
                .flatMap(info -> Mono
                        .fromCallable(() -> {
                            try (ReadableByteChannel input = Channels.newChannel(inputStream);
                                 FileChannel output = FileChannel.open(Paths.get(info.getSavePath()), FILE_CHANNEL_OPTIONS)) {
                                long size = (input instanceof FileChannel ? ((FileChannel) input).size() : Long.MAX_VALUE);
                                long totalWritten = 0;
                                while (totalWritten < size) {
                                    long written = output.transferFrom(input, totalWritten, size - totalWritten);
                                    if (written <= 0) {
                                        break;
                                    }
                                    totalWritten += written;
                                }
                                return info.getLocation();
                            }
                        })
                        .doOnSuccess((ignore) -> properties.applyFilePermission(new File(info.getSavePath())))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
