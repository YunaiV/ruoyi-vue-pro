package com.somle.ai.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiName {
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer birthYear;
    private String language;
}
