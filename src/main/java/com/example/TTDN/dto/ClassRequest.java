package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassRequest {
    private String className;
    private String schoolYear;
    private String enrollCode;
    private Long courseId;
}