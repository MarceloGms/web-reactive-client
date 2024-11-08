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

      // REQ 1: Media titles and release dates
      public void getMediaTitlesDates(String fileName) {
            fetchAllMedia()
                        .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date()
                                    + "\n---\n")
                        .transform(m -> fw.writeRows(m, fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Media titles and dates written to " + fileName)
                        );
      }

      // REQ 2: Media count
      public void countMedia(String fileName) {
            fetchAllMedia()
                        .count()
                        .map(count -> "Media count: " + count + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Media count written to " + fileName)
                        );
      }

      // REQ 3: Good rated media items
      public void countGoodRatedMedia(String fileName) {
            fetchAllMedia()
                        .filter(m -> m.getAverage_rating() > 8)
                        .count()
                        .map(count -> "Good rated media count: " + count + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Good rated media count written to " + fileName)
                        );
      }

      // REQ 4: Total subscribed media items
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

      // REQ 5: Media from the 80s sorted by average rating
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
                                    () -> System.out.println("Media from the 80s written to " + fileName)
                        );
      }

      // REQ 6: Average rating and standard deviation of all media items
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
                              float mean = acc[2] > 0 ? acc[0] / acc[2] : 0; // when theres no media put 0
                              float variance = acc[2] > 0 ? (acc[1] / acc[2]) - (mean * mean) : 0;
                              float stdDeviation = (float) Math.sqrt(variance);
                              return String.format("Mean: %.4f, Std Dev: %.4f\n---\n", mean, stdDeviation);
                        })
                        .transform(result -> fw.writeRows(result.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println(
                                                "Average rating and standard deviation written to " + fileName)
                        );
      }

      // REQ 7: Oldest media item
      public void oldestMedia(String fileName) {
            fetchAllMedia()
                        .reduce((m1, m2) -> m1.getRelease_date().isBefore(m2.getRelease_date()) ? m1 : m2)
                        .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date()
                                    + "\n---\n")
                        .transform(m -> fw.writeRows(m.flux(), fileName))
                        .subscribe(
                                    null,
                                    error -> System.err.println("Error writing " + fileName + ": " + error),
                                    () -> System.out.println("Oldest media written to " + fileName)
                        );
      }

      // REQ 8: Average number of users per media item
      public void averageUsersPerMedia(String fileName) {
            fetchAllMedia()
                        .flatMap(media ->
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
      
      // REQ 9: Name and number of users per media item, sorted by user age in descending order.
      public void getMediaNamesAndUserCountsSortedByAge(String fileName) {
            fetchAllMedia()
                        .flatMap(media ->
                              fetchUsersByMedia(media.getIdentifier())
                                    .flatMap(userId ->
                                          fetchAllUsers()
                                                .filter(user -> user.getIdentifier() == userId)
                                          )
                                    .sort((u1, u2) -> Integer.compare(u2.getAge(), u1.getAge()))
                                    .reduce("", (userInfo, user) -> {
                                          String userEntry = user.getName() + " (" + user.getAge() + ")";
                                          return userInfo.isEmpty() ? userEntry : userInfo + ", " + userEntry;
                                    })
                                    .map(userList -> {
                                          String result = "Media Title: " + media.getTitle() +
                                                            "\nUsers: " + (userList.isEmpty() ? "None" : userList) +
                                                            "\n---\n";
                                          return result;
                                    })
                        )
                        .transform(results -> fw.writeRows(results, fileName))
                        .subscribe(
                              null,
                              error -> System.err.println("Error writing " + fileName + ": " + error),
                              () -> System.out.println("Media names and user counts written to " + fileName)
                        );
      }

      // REQ 10: Complete data of all users by adding the names of subscribed media items
      public void getAllUsersWithSubscribedMedia(String fileName) {
            fetchAllUsers()
                        .flatMap(user -> 
                              fetchMediaByUser(user.getIdentifier())
                                    .flatMap(mediaId ->
                                          fetchAllMedia()
                                                .filter(media -> media.getIdentifier() == mediaId)
                                                .map(Media::getTitle)
                                    )
                                    .reduce("", (titles, title) -> titles.isEmpty() ? title : titles + ", " + title)
                                    .map(titles -> {
                                          String userInfo = "User ID: " + user.getIdentifier() +
                                                            "\nName: " + user.getName() +
                                                            "\nAge: " + user.getAge() +
                                                            "\nGender: " + user.getGender() +
                                                            "\nSubscribed Media: " + (titles.isEmpty() ? "None" : titles) +
                                                            "\n---\n";
                                          return userInfo;
                                    })
                        )
                        .transform(results -> fw.writeRows(results, fileName))
                        .subscribe(
                              null,
                              error -> System.err.println("Error writing " + fileName + ": " + error),
                              () -> System.out.println("All users with subscribed media written to " + fileName)
                        );
      }
}
