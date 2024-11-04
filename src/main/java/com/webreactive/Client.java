package com.webreactive;

import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Client {

	private static final String BASE_URL = "http://host.docker.internal:8080";
	// String MY_URI = "with delay";

   public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("\nPlease provide the output file names as program parameters.\n");
			return;
		}

		List<String> outputFiles = Arrays.asList(args);
		WebClient webClient = WebClient.create(BASE_URL);
		FileWriter fw = new FileWriter();
		MediaService ms = new MediaService(webClient, fw);
		UserService us = new UserService(webClient, fw);

		for (String outputFile : outputFiles) {
			switch (outputFile.toLowerCase()) {
				case "req1.txt":
					ms.getMediaTitlesDates(outputFile);
					break;
				case "req2.txt":
					ms.countMedia(outputFile);
					break;
				case "req3.txt":
					ms.countGoodRatedMedia(outputFile);
					break;
				case "req4.txt":
					// TODO
					break;
				case "req5.txt":
					ms.getMedia80s(outputFile);
					break;
				default:
				System.err.println("Unknown request: " + outputFile);
				break;
			}
		}

		System.out.println("Press Enter to exit");
		System.in.read();
		
	}
}

// primeiro req
// falta aqui alguma cena para proteger a ligaçao, deve se dar retry 3 vezes e
// dps cagar
// flux.cena(https://...).map(m -> ""+m.getfile()+ " " +
// m.getRDHC()+"").subscribe(m -> writeFile())
