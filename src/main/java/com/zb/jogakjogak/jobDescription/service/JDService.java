package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JDService {

    private final OpenAIResponseService openAIResponseService;

    /**
     * JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto analyze (JDRequestDto jdRequestDto) {
        // TODO: 이력서를 받아야 함. JD를 어떻게 전달 받을 지 고민 중....
        String resume = "5년차 백엔드 개발 경험. Java, Spring Boot 능숙. RESTful API 설계 및 개발 경험 다수. MSA 환경 경험";
        String jobDescription = "백엔드 개발자 채용. Java/Spring Boot 필수. RESTful API 개발 경험 우대. MSA 경험 우대. 적극적인 문제 해결 능력.";
        String result = openAIResponseService.sendRequest(resume, jobDescription, 0);
        return JDResponseDto.builder()
                .title(jdRequestDto.getTitle())
                .jdUrl(jdRequestDto.getJDUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .analysisResult(result)
                .memo("")
                .build();
    }
}
