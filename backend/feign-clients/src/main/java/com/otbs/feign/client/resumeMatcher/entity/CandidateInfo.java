package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;

@Data
public class CandidateInfo {
    private String name;
    private String email;
    private String phone;
    private Location location;
    private String linkedin;
    private String website;

    @Data
    public static class Location {
        private String city;
        private String state;
        private String country;
    }
}
