package com.webreactive;

import com.webreactive.entity.Media;

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

   // REQ 1
   public void getMediaTitlesDates(String fileName) {
      fetchAllMedia()
               .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date() + "\n---\n")
               .transform(m -> fw.writeRows(m, fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Media titles and dates written to " + fileName)
               );
   }

   // REQ 2
   public void countMedia(String fileName) {
      fetchAllMedia()
               .count()
               .map(count -> "Media count: " + count + "\n")
               .transform(m -> fw.writeRows(m.flux(), fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Media count written to " + fileName)
               );
   }

   // REQ 3
   public void countGoodRatedMedia(String fileName) {
      fetchAllMedia()
               .filter(m -> m.getAverage_rating() > 8)
               .count()
               .map(count -> "Good rated media count: " + count + "\n")
               .transform(m -> fw.writeRows(m.flux(), fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Good rated media count written to " + fileName)
               );
   }

   // REQ 4
   // TODO: Total count of media items that are subscribed

   // REQ 5
   public void getMedia80s(String fileName) {
      fetchAllMedia()
               .filter(m -> m.getRelease_date().getYear() >= 1980 && m.getRelease_date().getYear() < 1990)
               .sort((m1, m2) -> Double.compare(m2.getAverage_rating(), m1.getAverage_rating()))
               .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date() + "\nAverage Rating: " + media.getAverage_rating() + "\nType: " + media.typeConvert() + "\n---\n")
               .transform(m -> fw.writeRows(m, fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Media from the 80s written to " + fileName)
               );
   }

   // REQ 6
   public void ratingAvgStdMedia(String fileName) {
      fetchAllMedia()
               .map(media -> media.getAverage_rating())
               .reduce(new float[]{0, 0, 0}, (acc, rating) -> {
                  acc[0] += rating; // Sum of ratings
                  acc[1] += rating * rating; // Sum of squares
                  acc[2] += 1; // Count of ratings
                  return acc;
               })
               .map(acc -> {
                  float mean = acc[0] / acc[2];
                  float variance = (acc[1] / acc[2]) - (mean * mean);
                  float stdDeviation = (float) Math.sqrt(variance);
                  return String.format("Mean: %f, Std Dev: %f", mean, stdDeviation);
               })
               .transform(result -> fw.writeRows(result.flux(), fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Average rating and standard deviation written to " + fileName)
               );
   }

   // REQ 7
   public void oldestMedia(String fileName) {
      fetchAllMedia()
               .reduce((m1, m2) -> m1.getRelease_date().isBefore(m2.getRelease_date()) ? m1 : m2)
               .map(media -> "Title: " + media.getTitle() + "\nRelease Date: " + media.getRelease_date() + "\n---\n")
               .transform(m -> fw.writeRows(m.flux(), fileName))
               .subscribe(
                     null,
                     error -> System.err.println("Error writing " + fileName + ": " + error),
                     () -> System.out.println("Oldest media written to " + fileName)
               );
   }
}
