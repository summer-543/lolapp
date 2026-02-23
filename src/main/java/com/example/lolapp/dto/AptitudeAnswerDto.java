package com.example.lolapp.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AptitudeAnswerDto {
    private String type;
    private List<String> values;
}