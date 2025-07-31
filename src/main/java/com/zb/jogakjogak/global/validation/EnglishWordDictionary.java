package com.zb.jogakjogak.global.validation;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EnglishWordDictionary {

    // 운영체제에 독립적인 임시 디렉토리 경로 사용을 권장합니다.
    // System.getProperty("java.io.tmpdir")은 OS의 기본 임시 디렉토리를 반환합니다.
    private static final String CACHE_DIR = System.getProperty("java.io.tmpdir");
    private static final String CACHE_FILE_NAME = "word_cache.txt";
    private static final String WORDS_URL = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt";

    private final Set<String> validWords;
    private final Path cacheFilePath; // Path 객체로 관리

    public EnglishWordDictionary() {
        // 임시 디렉토리와 파일 이름을 결합하여 최종 경로 생성
        this.cacheFilePath = Paths.get(CACHE_DIR, CACHE_FILE_NAME);
        this.validWords = loadOrDownloadWords();
    }

    private Set<String> loadOrDownloadWords() {
        File cachedFile = cacheFilePath.toFile(); // Path 객체에서 File 객체 얻기

        try {
            if (cachedFile.exists()) {
                System.out.println("📄 로컬 캐시에서 단어 사전 로드 중: " + cacheFilePath);
                return Files.lines(cacheFilePath)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
            }

            System.out.println("🌐 인터넷에서 단어 사전 다운로드 중...");
            try (InputStream inputStream = new URL(WORDS_URL).openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                List<String> lines = reader.lines().collect(Collectors.toList());

                // **** 핵심 수정: 부모 디렉토리가 없으면 생성 ****
                Files.createDirectories(cacheFilePath.getParent()); // 파일의 부모 디렉토리를 생성합니다.

                // 로컬 캐시에 저장
                Files.write(cacheFilePath, lines);
                System.out.println("✅ 단어 사전 캐시 저장 완료: " + cacheFilePath);

                return lines.stream().map(String::toLowerCase).collect(Collectors.toSet());
            }

        } catch (IOException e) {
            // 더 구체적인 에러 메시지 포함
            throw new RuntimeException("단어 사전 로딩 실패: " + cacheFilePath + " 에서 파일을 읽거나 다운로드할 수 없습니다. 원인: " + e.getMessage(), e);
        }
    }

    public boolean isValid(String word) {
        return validWords.contains(word.toLowerCase());
    }
}

