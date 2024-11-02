package com.webreactive.entity;


import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media implements Serializable {
   private Long identifier;

   private String title;

   private LocalDate release_date;

   private float average_rating;

   private boolean type;
}
