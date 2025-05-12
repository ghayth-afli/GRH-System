package com.otbs.feign.client.resumeMatcher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeRequest {
    private String resume;

    @JsonProperty("resume_type")
    private String resumeType;  // pdf, docx, txt
}
