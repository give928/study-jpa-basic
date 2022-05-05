package com.give928.jpa.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class MemberDTO {
    private String username;
    private int age;
}
