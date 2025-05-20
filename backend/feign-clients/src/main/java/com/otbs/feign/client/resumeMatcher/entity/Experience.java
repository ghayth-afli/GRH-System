package com.otbs.feign.client.resumeMatcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Experience {
    private String company;
    private String title;
    private String location;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
    private List<String> responsibilities;
    private List<String> achievements;
}
