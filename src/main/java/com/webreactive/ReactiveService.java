package com.webreactive;

import com.webreactive.entity.Media;
import com.webreactive.entity.User;

import lombok.AllArgsConstructor;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

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

      private Flux<Long> fetchUsersByMedia(long id) {
            return webClient.get()
                        .uri("/relationship/media/" + id)
                        .retrieve()
                        .bodyToFlux(Long.class)
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                        .doOnError(error -> System.err.println("Error fetching media-users: " + error));
      }

      private Flux<Long> fetchMediaByUser(long id) {
            return webClient.get()
                        .uri("/relationship/user/" + id)
                        .retrieve()
                        .bodyToFlux(Long.class)
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                        .doOnError(error -> System.err.println("Error fetching user-media: " + error));
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
            fetchAllMedia()
                .flatMap(media -> 
                    fetchUsersByMedia(media.getIdentifier())
                        .count()
                        .filter(userCount -> userCount != 0)
                )
                .count()
                .map(count -> "Total subscribed media items: " + count + "\n")
                .transform(result -> fw.writeRows(result.flux(), fileName))
                .subscribe(
                    null,
                    error -> System.err.println("Error writing " + fileName + ": " + error),
                    () -> System.out.println("Total subscribed media count written to " + fileName)
                );
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
      public void averageUsersPerMedia(String fileName) {
            fetchAllMedia()
                  .flatMap(media -> // Fetch users for each media item
                        fetchUsersByMedia(media.getIdentifier())
                        .count()
                        .map(userCount -> new long[]{userCount, 1})
                  )
                  .reduce(new long[]{0, 0}, (acc, userAndMediaCount) -> {
                        acc[0] += userAndMediaCount[0]; // Total users
                        acc[1] += userAndMediaCount[1]; // Total media items
                        return acc;
                  })
                  .map(acc -> {
                        double average = acc[1] > 0 ? (double) acc[0] / acc[1] : 0.0;
                        return String.format("Average users per media: %.4f\n", average);
                  })
                  .transform(result -> fw.writeRows(result.flux(), fileName))
                  .subscribe(
                        null,
                        error -> System.err.println("Error writing " + fileName + ": " + error),
                        () -> System.out.println("Average users per media written to " + fileName)
                  );
      }
      
      // REQ 9
      // Name and number of users per media item, sorted by user age in descending
      // order.
      public void getMediaNamesAndUserCountsSortedByAge(String fileName) {
            fetchAllMedia()
                .flatMap(media -> 
                    fetchUsersByMedia(media.getIdentifier())
                        .flatMap(userId -> 
                            fetchAllUsers()
                                .filter(user -> user.getIdentifier() == userId)
                                .take(1) // Ensure only one user per userId
                        )
                        .sort((u1, u2) -> Integer.compare(u2.getAge(), u1.getAge())) // Sort users by age in descending order
                        .reduce(new StringBuilder("Media Title: " + media.getTitle() + "\nUser Count: "),
                                (acc, user) -> {
                                    // name and age
                                    acc.append(user.getName())
                                        .append(" (")
                                        .append(user.getAge())
                                        .append("), ");
                                    return acc;
                                })
                        .map(result -> result.substring(0, result.length() - 2) + "\n---\n") // Remove trailing comma, add separator
                )
                .transform(results -> fw.writeRows(results, fileName))
                .subscribe(
                    null,
                    error -> System.err.println("Error writing " + fileName + ": " + error),
                    () -> System.out.println("Media names and user counts written to " + fileName)
                );
        }

      // REQ 10: Complete data of all users by adding the names of subscribed media
      // items
      public void getAllUsersWithSubscribedMedia(String fileName) {
            /* // Fetch all media, users, and media-user relationships
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
                        */
      } 

}
