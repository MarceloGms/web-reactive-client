package com.webreactive;

import org.springframework.web.reactive.function.client.WebClient;
import com.webreactive.entity.User;

import java.io.IOException;

public class Client {
<<<<<<< HEAD:src/main/java/com/webreactive/Client.java
   public static void main(String[] args) throws IOException {
      String BASE_URL = "http://host.docker.internal:8080";
=======
	public static void main(String[] args) throws IOException {
		String BASE_URL = "http://host.docker.internal:8081";
>>>>>>> a1fbb6cd05cdbd8395afa096d126d336e3f38927:client/src/main/java/com/webreactive/Client.java
		String MY_URI = "/user";
		// String MY_URI = "with delay";
		WebClient.create(BASE_URL)
				.get()
				.uri(MY_URI)
				.retrieve()
				.bodyToFlux(User.class)
				.subscribe(System.out::println);

		System.out.println("Press Enter to exit");
		System.in.read();
	}
}

// primeiro req
// falta aqui alguma cena para proteger a ligaçao, deve se dar retry 3 vezes e
// dps cagar
// flux.cena(https://...).map(m -> ""+m.getfile()+ " " +
// m.getRDHC()+"").subscribe(m -> writeFile())
