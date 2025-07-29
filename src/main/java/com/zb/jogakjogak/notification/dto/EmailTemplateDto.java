package com.zb.jogakjogak.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 이메일 템플릿에 전달할 데이터를 담는 DTO
 */
@Data
@Builder
public class EmailTemplateDto {
    private String userName;
    private List<JdEmailDto> jdList;
    private String link;
    private String dailyMessage;
}

