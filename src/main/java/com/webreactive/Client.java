package com.webreactive;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.webreactive.entity.Media;
import com.webreactive.entity.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Client {

	private static final String BASE_URL = "http://host.docker.internal:8080";
	private static final String MEDIA_URI = "/media";
	private static final String USER_URI = "/user";
	// String MY_URI = "with delay";

   public static void main(String[] args) throws IOException {
		WebClient webClient = WebClient.create(BASE_URL);
		getMediaTitlesDates(webClient);

		System.out.println("Press Enter to exit");
		System.in.read();
		
	}

	public static Mono<Void> writeRows(Flux<String> rowsFlux, String filename) {
		DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		CharSequenceEncoder encoder = CharSequenceEncoder.textPlainOnly();

		Flux<DataBuffer> dataBufferFlux = rowsFlux.map(line ->
			encoder.encodeValue(line, bufferFactory, ResolvableType.NONE, null, null)
		);
		return DataBufferUtils.write(
			dataBufferFlux,
			Path.of("result/" + filename),
			StandardOpenOption.CREATE,
         StandardOpenOption.TRUNCATE_EXISTING
		);
	}

	private static void getMediaTitlesDates(WebClient webClient) {
		webClient.get()
				.uri(MEDIA_URI)
				.retrieve()
				.bodyToFlux(Media.class)
				.map(m -> "" + m.getTitle() + " " + m.getRelease_date() + "\n")
				.transform(m -> writeRows(m, "mediaTitlesDates.txt"))
				.subscribe(null,
					error -> System.err.println("Error writing mediaTitlesDates.txt: " + error),
					() -> System.out.println("Media titles and dates written to mediaTitlesDates.txt")
				 );
	}
}

// primeiro req
// falta aqui alguma cena para proteger a ligaçao, deve se dar retry 3 vezes e
// dps cagar
// flux.cena(https://...).map(m -> ""+m.getfile()+ " " +
// m.getRDHC()+"").subscribe(m -> writeFile())
