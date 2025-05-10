package com.otbs.feign.client.resumeMatcher.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeRequest {
    private String resume;
    private String resumeType;  // pdf, docx, txt
}
