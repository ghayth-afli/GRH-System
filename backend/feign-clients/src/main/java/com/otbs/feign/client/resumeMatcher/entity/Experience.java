package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;
import java.util.List;

@Data
public class Experience {
    private String company;
    private String title;
    private String location;
    private String startDate;
    private String endDate;
    private List<String> responsibilities;
    private List<String> achievements;
}
