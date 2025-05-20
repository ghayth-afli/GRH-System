package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;
import java.util.List;

@Data
public class Project {
    private String name;
    private String description;
    private List<String> technologies;
    private String url;
}
