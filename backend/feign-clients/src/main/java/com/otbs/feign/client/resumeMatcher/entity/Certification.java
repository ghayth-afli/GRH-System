package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;

@Data
public class Certification {
    private String name;
    private String issuer;
    private String date;
    private String expires;
}
