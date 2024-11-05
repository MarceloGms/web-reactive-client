package com.webreactive;

import com.webreactive.entity.Media;
import com.webreactive.entity.MediaUsers;
import com.webreactive.entity.User;

import lombok.AllArgsConstructor;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReactiveService {

      private final WebClient webClient;
      private final FileWriter fw;

      private Flux<Media> fetchAllMedia() {
            return webClient.get()
                        .uri("/media")
                        .retrieve()
                        .bodyToFlux(Media.class)
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                        .doOnError(error -> System.err.println("Error fetching media: " + error));
      }

      private Flux<User> fetchAllUsers() {
            return webClient.get()
                        .uri("/user")
                        .retrieve()
                        .bodyToFlux(User.class)
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                        .doOnError(error -> System.err.println("Error fetching users: " + error));
      }

      private Mono<List<List<Long>>> fetchMediaUsers() {
            return webClient.get()
                        .uri("/relationship/getMediaUsers")
                        .retrieve()
                        .bodyToFlux(Long.class) // Expecting Long values directly
                        .doOnNext(response -> System.out.println("Raw response: " + response))
                        .collectList()
                        .map(response -> {
                              List<List<Long>> mediaUsers = new ArrayList<>();
                              for (int i = 0; i < response.size(); i += 2) {
                                    mediaUsers.add(Arrays.asList(response.get(i), response.get(i + 1)));
                              }
                              return mediaUsers;
                        })
                        .doOnError(error -> System.err.println("Error fetching media-users: " + error));
      }

      // REQ 1
      public void getMediaTitlesDates(String fileName) {
            fetchAllMedia()
                        .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date()
                                    + "\n---\n")
                        .transform(m -> fw.writeRows(m, fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Media titles and dates written to " + fileName));
      }

      // REQ 2
      public void countMedia(String fileName) {
            fetchAllMedia()
                        .count()
                        .map(count -> "Media count: " + count + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Media count written to " + fileName));
      }

      // REQ 3
      public void countGoodRatedMedia(String fileName) {
            fetchAllMedia()
                        .filter(m -> m.getAverage_rating() > 8)
                        .count()
                        .map(count -> "Good rated media count: " + count + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Good rated media count written to " + fileName));
      }

      // REQ 4
      public void countSubscribedMedia(String fileName) {
            fetchMediaUsers() // Use fetchMediaUsers to get the media-user relationships
                        .map(mediaUsers -> mediaUsers.stream()
                                    .map(pair -> pair.get(0)) // Extract the media identifiers from the pairs
                                    .distinct() // Get distinct media identifiers
                                    .collect(Collectors.toSet())) // Collect into a Set to remove duplicates
                        .map(uniqueMediaIds -> "Subscribed media count: " + uniqueMediaIds.size() + "\n") // Count the
                                                                                                          // unique
                                                                                                          // media IDs
                        .transform(m -> fw.writeRows(m.flux(), fileName)) // Write the result to file
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Subscribed media count written to " + fileName));
      }

      // REQ 5
      public void getMedia80s(String fileName) {
            fetchAllMedia()
                        .filter(m -> m.getRelease_date().getYear() >= 1980 && m.getRelease_date().getYear() < 1990)
                        .sort((m1, m2) -> Double.compare(m2.getAverage_rating(), m1.getAverage_rating()))
                        .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date()
                                    + "\nAverage Rating: " + media.getAverage_rating() + "\nType: "
                                    + media.typeConvert() + "\n---\n")
                        .transform(m -> fw.writeRows(m, fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Media from the 80s written to " + fileName));
      }

      // REQ 6
      public void ratingAvgStdMedia(String fileName) {
            fetchAllMedia()
                        .map(media -> media.getAverage_rating())
                        .reduce(new float[] { 0, 0, 0 }, (acc, rating) -> {
                              acc[0] += rating; // Sum of ratings
                              acc[1] += rating * rating; // Sum of squares
                              acc[2] += 1; // Count of ratings
                              return acc;
                        })
                        .map(acc -> {
                              float mean = acc[0] / acc[2];
                              float variance = (acc[1] / acc[2]) - (mean * mean);
                              float stdDeviation = (float) Math.sqrt(variance);
                              return String.format("Mean: %f, Std Dev: %f\n---\n", mean, stdDeviation);
                        })
                        .transform(result -> fw.writeRows(result.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println(
                                                "Average rating and standard deviation written to " + fileName));
      }

      // REQ 7
      public void oldestMedia(String fileName) {
            fetchAllMedia()
                        .reduce((m1, m2) -> m1.getRelease_date().isBefore(m2.getRelease_date()) ? m1 : m2)
                        .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date()
                                    + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Oldest media written to " + fileName));
      }

      // REQ 8
      // The average number of users per media item. Note that some media items may
      // not have users and vice versa.

      public void averageUsersPerMedia(String fileName) {
            fetchMediaUsers()
                        .map(mu -> {
                              // Create a Map to hold media identifiers and their user counts
                              Map<Long, Set<Long>> mediaUserMap = new HashMap<>();

                              // Fill the map with media-user pairs
                              for (List<Long> pair : mu) {
                                    Long mediaId = pair.get(0); // media identifier
                                    Long userId = pair.get(1); // user identifier

                                    // Add the userId to the set of users for this mediaId
                                    mediaUserMap.computeIfAbsent(mediaId, k -> new HashSet<>()).add(userId);
                              }

                              // Calculate the total number of users and the average
                              double totalUsers = mediaUserMap.values().stream()
                                          .mapToInt(Set::size) // Count the distinct users for each media item
                                          .sum();

                              double average = mediaUserMap.size() > 0 ? totalUsers / mediaUserMap.size() : 0.0; // Calculate
                                                                                                                 // average

                              return "Average users per media: " + average + "\n";
                        })
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Average users per media written to " + fileName));
      }

      // REQ 9
      // Name and number of users per media item, sorted by user age in descending
      // order.
      public void getMediaNamesAndUserCountsSortedByAge(String fileName) {
            // Fetch all media, users, and media-user relationships
            Flux<Media> mediaFlux = fetchAllMedia();
            Flux<User> userFlux = fetchAllUsers();
            Mono<List<List<Long>>> mediaUsersFlux = fetchMediaUsers();

            mediaUsersFlux.flatMap(mediaUsers -> {
                  return userFlux.collectList().flatMap(userList -> {
                        // Create a map to hold media and user information
                        Map<Long, List<User>> mediaUserMap = new HashMap<>();

                        // Populate the media-user map
                        for (List<Long> pair : mediaUsers) {
                              Long mediaId = pair.get(0); // media identifier
                              Long userId = pair.get(1); // user identifier

                              // Find the user by ID
                              User user = userList.stream()
                                          .filter(u -> u.getIdentifier() == userId)
                                          .findFirst()
                                          .orElse(null);

                              if (user != null) {
                                    mediaUserMap.computeIfAbsent(mediaId, k -> new ArrayList<>()).add(user);
                              }
                        }

                        // Create a list to hold the results
                        List<String> results = new ArrayList<>();

                        // For each media item, get the name and user count, and sort users by age
                        mediaFlux.collectList().subscribe(mediaList -> {
                              for (Media media : mediaList) {
                                    List<User> users = mediaUserMap.get(media.getIdentifier());
                                    if (users != null && !users.isEmpty()) {
                                          // Sort users by age in descending order
                                          List<User> sortedUsers = users.stream()
                                                      .sorted((u1, u2) -> Integer.compare(u2.getAge(), u1.getAge()))
                                                      .collect(Collectors.toList());

                                          // Prepare the output string
                                          String result = "Media Title: " + media.getTitle() +
                                                      "\nUser Count: " + sortedUsers.size() +
                                                      "\nUsers (sorted by age): " +
                                                      sortedUsers.stream()
                                                                  .map(User::getName)
                                                                  .collect(Collectors.joining(", "))
                                                      +
                                                      "\n---\n";
                                          results.add(result);
                                    }
                              }

                              // Write results to the specified file
                              fw.writeRows(Flux.fromIterable(results), fileName)
                                          .subscribe(
                                                      null,
                                                      error -> System.err
                                                                  .println("Error writing " + fileName + ": " + error),
                                                      () -> System.out.println("Media names and user counts written to "
                                                                  + fileName));
                        });

                        return Mono.empty(); // Return an empty Mono to satisfy the flatMap
                  });
            }).subscribe(
                        null,
                        error -> System.err.println("Error in getMediaNamesAndUserCountsSortedByAge: " + error));
      }

      // REQ 10: Complete data of all users by adding the names of subscribed media
      // items
      public void getAllUsersWithSubscribedMedia(String fileName) {
            // Fetch all media, users, and media-user relationships
            Flux<Media> mediaFlux = fetchAllMedia();
            Flux<User> userFlux = fetchAllUsers();
            Mono<List<List<Long>>> mediaUsersFlux = fetchMediaUsers();

            mediaUsersFlux.flatMap(mediaUsers -> {
                  return mediaFlux.collectList().flatMap(mediaList -> {
                        return userFlux.collectList().flatMap(userList -> {
                              // Create a map of mediaId to media title
                              Map<Long, String> mediaMap = mediaList.stream()
                                          .collect(Collectors.toMap(Media::getIdentifier, Media::getTitle));

                              // Create a map to store user subscriptions
                              Map<Long, Set<String>> userSubscriptions = new HashMap<>();

                              // Populate user subscriptions with media titles
                              for (List<Long> pair : mediaUsers) {
                                    Long mediaId = pair.get(0); // Media identifier
                                    Long userId = pair.get(1); // User identifier
                                    String mediaTitle = mediaMap.get(mediaId);

                                    if (mediaTitle != null) {
                                          userSubscriptions.computeIfAbsent(userId, k -> new HashSet<>())
                                                      .add(mediaTitle);
                                    }
                              }

                              // Build the complete user data with subscribed media names, age, and gender
                              List<String> results = userList.stream()
                                          .map(user -> {
                                                Set<String> subscribedMedia = userSubscriptions
                                                            .getOrDefault(user.getIdentifier(), Collections.emptySet());
                                                return "User: " + user.getName() +
                                                            "\nAge: " + user.getAge() +
                                                            "\nGender: " + user.getGender() +
                                                            "\nSubscribed Media: " + String.join(", ", subscribedMedia)
                                                            +
                                                            "\n---\n";
                                          })
                                          .collect(Collectors.toList());

                              // Write the results to the specified file
                              return fw.writeRows(Flux.fromIterable(results), fileName);
                        });
                  });
            }).subscribe(
                        null,
                        error -> System.err.println("Error writing " + fileName + ": " + error),
                        () -> System.out.println("All users with subscribed media written to " + fileName));
      }

}
