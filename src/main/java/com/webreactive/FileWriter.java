package com.webreactive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// https://stackoverflow.com/questions/74870351/spring-webflux-project-reactor-how-to-write-data-from-flux-to-text-file
@NoArgsConstructor
public class FileWriter {
   public Mono<Void> writeRows(Flux<String> rowsFlux, String filename) {
        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        CharSequenceEncoder encoder = CharSequenceEncoder.textPlainOnly();

        Flux<DataBuffer> dataBufferFlux = rowsFlux.map(line ->
                encoder.encodeValue(line, bufferFactory, ResolvableType.NONE, null, null)
        );

        Path path = Path.of("result/" + filename);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            return Mono.error(e);
        }

        return DataBufferUtils.write(
                dataBufferFlux,
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
