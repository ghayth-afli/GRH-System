package com.otbs.feign.client.resumeMatcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Education {
    private String degree;
    @JsonProperty("field_of_study")
    private String fieldOfStudy;
    private String institution;
    private String location;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
}
