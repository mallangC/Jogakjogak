package com.zb.jogakjogak.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
public class MeaningfulTextValidator implements ConstraintValidator<MeaningfulText, String> {

    // 1. 5회 이상 반복되는 동일 문자 (한글, 영문, 특수문자 모두 포함)
    // 2. 동일 알파벳 4회 이상 연속 반복 (예: aaaa, bbbb)
    // 3. 5회 이상 반복되는 한글 자음/모음 (예: ㅋㅋㅋㅋㅋ, ㅏㅏㅏㅏㅏ)
    // 4. 13자 이상 연속되는 숫자 (전화번호 등 제외한 긴 숫자 나열)
    private static final Pattern REPEATING_CHARS_PATTERN = Pattern.compile(
            "(.)\\1{4,}" // 5회 이상 반복되는 동일 문자 (예: "aaaaa", "!!!!!")
            + "|([a-zA-Z])\\1{3,}" // 동일 알파벳 4회 이상 연속 반복 (예: "aaaa", "eeee")
            + "|[ㄱ-ㅎㅏ-ㅣ]{5,}" // 5회 이상 반복되는 한글 자음/모음 (예: "ㅋㅋㅋㅋㅋ", "ㅏㅏㅏㅏㅏ")
            + "|[0-9]{13,}"    // 13자 이상 연속되는 숫자 (의미 없는 긴 숫자 나열을 잡기 위함)
    );

    // 한글 무작위/의미 없는 패턴
    // - "ㅁㄴㅇㅁㄴㅇ", "ㅂㅈㄷㄱㅂㅈㄷㄱ" 등 자모 번갈아 나오는 패턴
    // - (?: 패턴은 그룹을 만들지 않고 매칭만 합니다. 성능 최적화)
    private static final Pattern KOREAN_GIBBERISH_PATTERN = Pattern.compile(
            "(?:[ㄱ-ㅎㅏ-ㅣ][^ㄱ-ㅎㅏ-ㅣ]){3,}|(?:[ㄱ-ㅎㅏ-ㅣ][ㄱ-ㅎㅏ-ㅣ]){2,}[ㄱ-ㅎㅏ-ㅣ]?"
    );


    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[^a-zA-Z]+");

    private final EnglishWordDictionary englishWordDictionary;

    public MeaningfulTextValidator(EnglishWordDictionary englishWordDictionary) {
        this.englishWordDictionary = englishWordDictionary;
    }

    @Override
    public void initialize(MeaningfulText constraintAnnotation) {
        // 초기화 로직 (필요 시)
    }

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context) {

        if (text == null) {
            return true; // null 값은 @NotBlank에게 맡김
        }

        String trimmedText = text.trim();


        // 명백한 반복 문자 패턴 검사 (가장 빠르고 확실한 필터링)
        if (REPEATING_CHARS_PATTERN.matcher(trimmedText).find()) {
            return false;
        }

        // 한글 무작위 입력 패턴 검사
        if (containsKorean(trimmedText) && KOREAN_GIBBERISH_PATTERN.matcher(trimmedText).find()) {
            return false;
        }

        // 실제 단어 사전 기반 유효성 검사 (영문 텍스트에만 적용)
        if (containsAlphabets(trimmedText)) {
            String[] words = WORD_SPLIT_PATTERN.split(trimmedText.toLowerCase());

            if (trimmedText.length() >= 10 && englishWordDictionary != null) {
                long validWordCount = Arrays.stream(words)
                        .filter(word -> word.length() > 1 && englishWordDictionary.isValid(word))
                        .count();
                if (validWordCount == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    // 텍스트에 영문 알파벳이 포함되어 있는지 확인
    private boolean containsAlphabets(String text) {
        return text.matches(".*[a-zA-Z]+.*");
    }

    // 텍스트에 한글 (완성형 한글 또는 자모)이 포함되어 있는지 확인
    private boolean containsKorean(String text) {
        // 한글 유니코드 범위: AC00–D7A3 (완성형 한글), 1100–11FF (초성/중성/종성), 3130–318F (호환용 자모)
        return text.matches(".*[\\uAC00-\\uD7A3\\u1100-\\u11FF\\u3130-\\u318F]+.*");
    }
}
