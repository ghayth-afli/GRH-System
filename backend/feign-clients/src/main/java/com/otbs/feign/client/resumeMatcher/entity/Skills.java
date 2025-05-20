package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;
import java.util.List;

@Data
public class Skills {
    private List<String> technical;
    private List<String> soft;
}
