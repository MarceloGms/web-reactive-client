package com.webreactive;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Client {

	private static final String BASE_URL = "http://host.docker.internal:8080";
	// String MY_URI = "with delay";

   public static void main(String[] args) throws IOException {
		WebClient webClient = WebClient.create(BASE_URL);
		FileWriter fw = new FileWriter();
		MediaService ms = new MediaService(webClient, fw);
		UserService us = new UserService(webClient, fw);

		// REQ 1
		ms.getMediaTitlesDates("1:mediaTitlesDates.txt");

		// REQ 2
		ms.countMedia("2:mediaCount.txt");

		// REQ 3
		ms.countGoodRatedMedia("3:goodRatedMediaCount.txt");

		// REQ 4

		// REQ 5
		ms.getMedia80s("5:media80s.txt");

		System.out.println("Press Enter to exit");
		System.in.read();
		
	}
}

// primeiro req
// falta aqui alguma cena para proteger a ligaçao, deve se dar retry 3 vezes e
// dps cagar
// flux.cena(https://...).map(m -> ""+m.getfile()+ " " +
// m.getRDHC()+"").subscribe(m -> writeFile())
