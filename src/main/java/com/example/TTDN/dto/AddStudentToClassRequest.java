package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddStudentToClassRequest {
    private Long classId;
    private Long studentProfileId;
}