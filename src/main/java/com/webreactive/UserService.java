package com.webreactive;

import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService {
   private final WebClient webClient;
   private final FileWriter fw;
   private static final String USER_URI = "/user";


}
