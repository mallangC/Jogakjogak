package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.resume.repository.SkillWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillWordService {

    private final SkillWordRepository skillWordRepository;

    private static final List<String> EXCEPTION_LIST = Arrays.asList(
            "C", "R", "GO", "AI", "UX", "UI", "QA", "QC",
            "PM", "BI", "ML", "DL", "AWS", "GCP", "CAD"
    );

    public List<String> getAutocompleteSuggestions(String query) {
        if (query == null || query.isEmpty()) {
            return List.of();
        }

        if(isEnglish(query) && query.length() < 3 &&
           !EXCEPTION_LIST.contains(query.toUpperCase())) {
            return List.of();
        }

        if(isKorean(query) && query.length() < 2) {
            return List.of();
        }

        List<String> skills = skillWordRepository.findContentsByContentLike(query);

        return skills.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .limit(8)
                .collect(Collectors.toList());

    }

    private boolean isEnglish(String query) {
        return query.matches("^[a-zA-Z\\s#+]*$");
    }

    private boolean isKorean(String query) {
        return query.matches("^[가-힣]*$");
    }
}
