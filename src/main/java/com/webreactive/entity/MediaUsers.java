package com.webreactive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaUsers {

    private Long mediaIdentifier;

    private Long usersIdentifier;
}