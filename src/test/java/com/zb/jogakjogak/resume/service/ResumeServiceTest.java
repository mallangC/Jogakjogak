package com.zb.jogakjogak.resume.service;


import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.global.exception.ResumeErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.CareerDto;
import com.zb.jogakjogak.resume.domain.requestDto.EducationDto;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeAddRequestDto;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeGetResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Career;
import com.zb.jogakjogak.resume.entity.Education;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.entity.Skill;
import com.zb.jogakjogak.resume.repository.CareerRepository;
import com.zb.jogakjogak.resume.repository.EducationRepository;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.resume.repository.SkillRepository;
import com.zb.jogakjogak.resume.type.EducationLevel;
import com.zb.jogakjogak.resume.type.EducationStatus;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private ResumeService resumeService;

    private Resume sampleResume;
    private ResumeRequestDto sampleRequestDto;
    private Faker faker;
    private Member mockMember;


    @BeforeEach
    void setUp() {
        faker = new Faker();

        mockMember = Member.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();


        sampleResume = Resume.builder()
                .id(1L)
                .title(faker.job().title())
                .content("기존 내용")
                .member(mockMember)
                .build();

        sampleRequestDto = ResumeRequestDto.builder()
                .title("새로운 이름")
                .content("새로운 내용")
                .build();
    }

    @DisplayName("이력서 등록 테스트")
    @Test
    void createResume_success() {
        // Given
        String fixedUserName = "testUser123";
        String testName = "테스트 이력서";
        String testContent = "이것은 테스트 내용입니다.";

        Member mockMember = Member.builder()
                .id(1L)
                .username(fixedUserName)
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(null)
                .build();

        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title(testName)
                .content(testContent)
                .build();

        Resume mockResume = Resume.builder()
                .id(1L)
                .title(testName)
                .content(testContent)
                .member(mockMember)
                .build();

        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);

        // When
        ResumeResponseDto responseDto = resumeService.register(requestDto, mockMember);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getResumeId()).isEqualTo(1L);
        assertThat(responseDto.getTitle()).isEqualTo(testName);
        assertThat(responseDto.getContent()).isEqualTo(testContent);

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @DisplayName("이력서 등록 실패 - 회원이 이미 이력서를 가지고 있을 때")
    @Test
    void createResume_alreadyHaveResume_throwsAuthException() {
        // Given
        String fixedUserName = "testUserWithResume";
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("새 이력서")
                .content("새 내용")
                .build();

        Member memberWithResume = Member.builder()
                .id(2L)
                .username(fixedUserName)
                .email("test2@example.com")
                .password("pass")
                .role(Role.USER)
                .resume(Resume.builder().id(300L).build())
                .build();

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () -> {
            resumeService.register(requestDto, memberWithResume);
        });

        assertThat(exception.getMemberErrorCode()).isEqualTo(MemberErrorCode.ALREADY_HAVE_RESUME);
        verify(resumeRepository, never()).save(any(Resume.class));
    }

    @Test
    @DisplayName("이력서 수정 성공 테스트 - 200 OK 예상")
    void modify_success() {
        //Given
        Resume saveResume = Resume.builder()
                .id(1L)
                .title(sampleRequestDto.getTitle())
                .content(sampleRequestDto.getContent())
                .member(mockMember)
                .build();

        when(resumeRepository.findResumeWithMemberByIdAndMemberId(1L, mockMember.getId())).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(saveResume);

        //When
        ResumeResponseDto result = resumeService.modify(1L, sampleRequestDto, mockMember);

        //Then
        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(1L, mockMember.getId());

        assertEquals(sampleRequestDto.getTitle(), sampleResume.getTitle());
        assertEquals(sampleRequestDto.getContent(), sampleResume.getContent());

        assertNotNull(result);
        assertEquals(sampleResume.getId(), result.getResumeId());
        assertEquals(sampleRequestDto.getTitle(), result.getTitle());
        assertEquals(sampleRequestDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("이력서 수정 실패 테스트 - 이력서를 찾을 수 없음")
    void modify_fail_notFoundResume() {
        //Given
        Long nonExistentResumeId = 99L;
        when(resumeRepository.findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId())).thenReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.modify(nonExistentResumeId, sampleRequestDto, mockMember);
        });

        // 예외 메시지 또는 에러 코드 검증
        assertEquals(ResumeErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());

        // findById 메소드가 호출되었는지 확인
        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId());
    }

    @Test
    @DisplayName("이력서 조회 성공 테스트 - 200 OK 예상")
    void get_success() {
        //Given
        when(resumeRepository.findResumeWithMemberByIdAndMemberId(1L, mockMember.getId())).thenReturn(Optional.of(sampleResume));

        //When
        ResumeResponseDto result = resumeService.get(1L, mockMember);

        //Then
        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(1L, mockMember.getId());

        assertNotNull(result);
        assertEquals(sampleResume.getId(), result.getResumeId());
        assertEquals(sampleResume.getTitle(), result.getTitle());
        assertEquals(sampleResume.getContent(), result.getContent());
    }

    @Test
    @DisplayName("이력서 조회 실패 테스트 - 이력서를 찾을 수 없음")
    void get_fail_notFoundResume() {
        //Given
        Long nonExistentResumeId = 99L;
        when(resumeRepository.findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId())).thenReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.get(nonExistentResumeId, mockMember);
        });

        // 예외 메시지 또는 에러 코드 검증
        assertEquals(ResumeErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());

        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId());
    }

    @DisplayName("이력서 삭제 성공 테스트")
    @Test
    void deleteResume_success() {
        // Given
        Long resumeIdToDelete = 1L;
        when(resumeRepository.findResumeWithMemberByIdAndMemberId(resumeIdToDelete, mockMember.getId())).thenReturn(Optional.of(sampleResume));

        // When
        resumeService.delete(resumeIdToDelete, mockMember);

        // Then
        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(resumeIdToDelete, mockMember.getId());
        assertThat(mockMember.getResume()).isNull();
    }

    @DisplayName("이력서 삭제 실패 테스트 - 이력서를 찾을 수 없음")
    @Test
    void deleteResume_fail_notFound() {
        // Given
        Long nonExistentResumeId = 99L;
        given(resumeRepository.findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId())).willReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.delete(nonExistentResumeId, mockMember);
        });

        assertEquals(ResumeErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());

        verify(resumeRepository, times(1)).findResumeWithMemberByIdAndMemberId(nonExistentResumeId, mockMember.getId());
        verify(resumeRepository, times(0)).delete(any(Resume.class));
    }

    @Test
    @DisplayName("(v2) 이력서 추가 성공 - 신입")
    void registerV2_success_newcomer() {
        String fixedUserName = "testUser123";
        String testContent = faker.lorem().sentence(3);
        boolean isNewcomer = true;

        Member mockMember = Member.builder()
                .id(1L)
                .username(fixedUserName)
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(null)
                .build();

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(testContent)
                .isNewcomer(isNewcomer)
                .build();

        Resume mockResume = Resume.builder()
                .id(1L)
                .content(testContent)
                .member(mockMember)
                .isNewcomer(isNewcomer)
                .build();


        given(memberRepository.findByUsername(fixedUserName))
                .willReturn(Optional.of(mockMember));
        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);

        // When
        ResumeGetResponseDto responseDto = resumeService.registerV2(requestDto, mockMember);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getContent()).isEqualTo(testContent);

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    @DisplayName("(v2) 이력서 추가 성공 - 신입 - 학력,스킬 추가")
    void registerV2_success_newcomer_plus_education_and_skill() {
        String fixedUserName = "testUser123";
        String testContent = faker.lorem().sentence(3);
        boolean isNewcomer = true;

        Member mockMember = Member.builder()
                .id(1L)
                .username(fixedUserName)
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(null)
                .build();

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(testContent)
                .isNewcomer(isNewcomer)
                .educationList(new ArrayList<>(List.of(
                        EducationDto.builder()
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        EducationDto.builder()
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new ArrayList<>(List.of(
                        "조각", "조가악"
                )))
                .build();

        Resume mockResume = Resume.builder()
                .id(1L)
                .content(testContent)
                .member(mockMember)
                .isNewcomer(isNewcomer)
                .build();

        given(memberRepository.findByUsername(fixedUserName))
                .willReturn(Optional.of(mockMember));
        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);

        // When
        ResumeGetResponseDto responseDto = resumeService.registerV2(requestDto, mockMember);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getContent()).isEqualTo(testContent);

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    @DisplayName("(v2) 이력서 추가 성공 - 경력 - 경력,학력,스킬 추가")
    void registerV2_success_plus_career_education_and_skill() {
        String fixedUserName = "testUser123";
        String testContent = faker.lorem().sentence(3);
        boolean isNewcomer = false;

        Member mockMember = Member.builder()
                .id(1L)
                .username(fixedUserName)
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(null)
                .build();

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(testContent)
                .isNewcomer(isNewcomer)
                .careerList(new ArrayList<>(List.of(
                        CareerDto.builder()
                                .companyName("조각조각")
                                .isWorking(true)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new ArrayList<>(List.of(
                        EducationDto.builder()
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        EducationDto.builder()
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new ArrayList<>(List.of(
                        "조각", "조가악"
                )))
                .build();

        Resume mockResume = Resume.builder()
                .id(1L)
                .content(testContent)
                .member(mockMember)
                .isNewcomer(isNewcomer)
                .build();

        given(memberRepository.findByUsername(fixedUserName))
                .willReturn(Optional.of(mockMember));
        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);

        // When
        ResumeGetResponseDto responseDto = resumeService.registerV2(requestDto, mockMember);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getContent()).isEqualTo(testContent);

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    @DisplayName("(v2) 이력서 추가 실패 - 경력인데 경력에 아무것도 없을 때")
    void registerV2_fail_not_entered_career() {
        String fixedUserName = "testUser123";
        String testContent = faker.lorem().sentence(3);
        boolean isNewcomer = false;

        Member mockMember = Member.builder()
                .id(1L)
                .username(fixedUserName)
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(null)
                .build();

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(testContent)
                .isNewcomer(isNewcomer)
                .build();

        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.registerV2(requestDto, mockMember);
        });

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ResumeErrorCode.NOT_ENTERED_CAREER);
        verify(resumeRepository, times(0)).save(any(Resume.class));
    }

    @Test
    @DisplayName("(v2)이력서 조회 성공 테스트")
    void getResume_successV2() {
        Resume resume = Resume.builder()
                .content(faker.lorem().sentence(3))
                .member(mockMember)
                .isNewcomer(false)
                .careerList(new HashSet<>(List.of(
                        Career.builder()
                                .id(1L)
                                .companyName("조각조각")
                                .isWorking(true)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new HashSet<>(List.of(
                        Education.builder()
                                .id(1L)
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        Education.builder()
                                .id(2L)
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new HashSet<>(List.of(
                        Skill.builder()
                                .id(1L)
                                .content("조각")
                                .build()
                )))
                .build();

        //Given
        when(resumeRepository.findResumeWithCareerAndEducationAndSkill(mockMember.getId()))
                .thenReturn(Optional.of(resume));

        //When
        ResumeGetResponseDto result = resumeService.getResumeV2(mockMember);

        //Then
        verify(resumeRepository, times(1)).findResumeWithCareerAndEducationAndSkill(mockMember.getId());

        assertNotNull(result);
        assertEquals(resume.getContent(), result.getContent());
        assertEquals(resume.getMember(), mockMember);
        assertEquals("조각", result.getSkillList().get(0));

        Optional<Education> filterEducation = resume.getEducationList().stream()
                .filter(education -> Objects.equals(education.getMajorField(), "조각고등학교"))
                .findFirst();

        Optional<EducationDto> filterEducationFromResult = result.getEducationDtoList().stream()
                .filter(education -> Objects.equals(education.getMajorField(), "조각고등학교"))
                .findFirst();

        assertTrue(filterEducation.isPresent());
        assertTrue(filterEducationFromResult.isPresent());
        assertEquals(filterEducation.get().getLevel(), filterEducationFromResult.get().getLevel());
        assertEquals(filterEducation.get().getMajorField(), filterEducationFromResult.get().getMajorField());
        assertEquals(filterEducation.get().getStatus(), filterEducationFromResult.get().getStatus());
    }

    @Test
    @DisplayName("(v2)이력서 조회 성공 테스트 - 신입이고 이력서 상세가 아무것도 없을 경우")
    void getResume_successV2_2() {
        Resume resume = Resume.builder()
                .content(null)
                .member(mockMember)
                .isNewcomer(true)
                .build();

        //Given
        when(resumeRepository.findResumeWithCareerAndEducationAndSkill(mockMember.getId()))
                .thenReturn(Optional.of(resume));

        //When
        ResumeGetResponseDto result = resumeService.getResumeV2(mockMember);

        //Then
        verify(resumeRepository, times(1)).findResumeWithCareerAndEducationAndSkill(mockMember.getId());

        assertNotNull(result);
        assertEquals(resume.getContent(), result.getContent());
        assertEquals(resume.getMember(), mockMember);
        assertTrue(result.isNewcomer());
    }

    @Test
    @DisplayName("(v2)이력서 수정 성공 테스트")
    void modifyV2_success() {
        //Given
        Resume resume = Resume.builder()
                .content(faker.lorem().sentence(3))
                .member(mockMember)
                .isNewcomer(false)
                .careerList(new HashSet<>(List.of(
                        Career.builder()
                                .id(1L)
                                .companyName("조각조각")
                                .isWorking(true)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new HashSet<>(List.of(
                        Education.builder()
                                .id(1L)
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        Education.builder()
                                .id(2L)
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new HashSet<>(List.of(
                        Skill.builder()
                                .id(1L)
                                .content("조각")
                                .build()
                )))
                .build();

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(faker.lorem().sentence(3))
                .isNewcomer(false)
                .careerList(new ArrayList<>(List.of(
                        CareerDto.builder()
                                .companyName("조각조각update")
                                .isWorking(false)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .quitAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new ArrayList<>(List.of(
                        EducationDto.builder()
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교update")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        EducationDto.builder()
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과update")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new ArrayList<>(List.of(
                        "조각update", "조가악update"
                )))
                .build();
        mockMember.setResume(resume);
        when(resumeRepository.findResumeWithCareerAndEducationAndSkill(mockMember.getId())).thenReturn(Optional.of(resume));
        when(careerRepository.saveAll(any())).thenReturn(List.of(
                Career.builder()
                        .id(1L)
                        .companyName("조각조각update")
                        .isWorking(true)
                        .joinedAt(LocalDate.of(2020, 1, 1))
                        .workPerformance(faker.lorem().paragraph(2))
                        .build()));
        when(educationRepository.saveAll(any())).thenReturn(List.of(
                Education.builder()
                        .id(1L)
                        .level(EducationLevel.HIGH_SCHOOL)
                        .majorField("조각고등학교update")
                        .status(EducationStatus.GRADUATED)
                        .build(),
                Education.builder()
                        .id(2L)
                        .level(EducationLevel.BACHELOR)
                        .majorField("조각대학교 조각학과update")
                        .status(EducationStatus.GRADUATED)
                        .build()));
        when(skillRepository.saveAll(any())).thenReturn(List.of(
                Skill.builder()
                        .id(1L)
                        .content("조각update")
                        .build(),
                Skill.builder()
                        .id(2L)
                        .content("조가악update")
                        .build()));

        //When
        ResumeGetResponseDto result = resumeService.modifyV2(requestDto, mockMember);

        //Then
        verify(resumeRepository, times(1)).findResumeWithCareerAndEducationAndSkill(mockMember.getId());

        assertNotNull(result);
        assertEquals(requestDto.getContent(), result.getContent());
        assertEquals(requestDto.getEducationList().get(0).getMajorField(), result.getEducationDtoList().get(0).getMajorField());
        assertEquals(requestDto.getSkillList().get(0), result.getSkillList().get(0));
        assertEquals(requestDto.getCareerList().get(0).getCompanyName(), result.getCareerDtoList().get(0).getCompanyName());
    }


    @Test
    @DisplayName("(v2)이력서 수정 실패 테스트 - resume을 찾을 수 없음")
    void modifyV2_failure1() {
        //Given
        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(faker.lorem().sentence(3))
                .isNewcomer(false)
                .careerList(new ArrayList<>(List.of(
                        CareerDto.builder()
                                .companyName("조각조각update")
                                .isWorking(true)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new ArrayList<>(List.of(
                        EducationDto.builder()
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교update")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        EducationDto.builder()
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과update")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new ArrayList<>(List.of(
                        "조각update", "조가악update"
                )))
                .build();

        //When
        ResumeException exception = assertThrows(ResumeException.class, () ->
                resumeService.modifyV2(requestDto, mockMember));

        //Then
        verify(resumeRepository, times(0)).findResumeWithCareerAndEducationAndSkill(mockMember.getId());
        assertThat(exception.getErrorCode()).isEqualTo(ResumeErrorCode.NOT_FOUND_RESUME);
    }

    @Test
    @DisplayName("(v2)이력서 수정 실패 테스트 - 신입이 아닌데 경력을 작성안함")
    void modifyV2_failure2() {
        //Given
        Resume resume = Resume.builder()
                .content(faker.lorem().sentence(3))
                .member(mockMember)
                .isNewcomer(false)
                .careerList(new HashSet<>(List.of(
                        Career.builder()
                                .id(1L)
                                .companyName("조각조각")
                                .isWorking(true)
                                .joinedAt(LocalDate.of(2020, 1, 1))
                                .workPerformance(faker.lorem().paragraph(2))
                                .build()
                )))
                .educationList(new HashSet<>(List.of(
                        Education.builder()
                                .id(1L)
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        Education.builder()
                                .id(2L)
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new HashSet<>(List.of(
                        Skill.builder()
                                .id(1L)
                                .content("조각")
                                .build()
                )))
                .build();
        mockMember.setResume(resume);

        ResumeAddRequestDto requestDto = ResumeAddRequestDto.builder()
                .content(faker.lorem().sentence(3))
                .isNewcomer(false)
                .educationList(new ArrayList<>(List.of(
                        EducationDto.builder()
                                .level(EducationLevel.HIGH_SCHOOL)
                                .majorField("조각고등학교update")
                                .status(EducationStatus.GRADUATED)
                                .build(),
                        EducationDto.builder()
                                .level(EducationLevel.BACHELOR)
                                .majorField("조각대학교 조각학과update")
                                .status(EducationStatus.GRADUATED)
                                .build()
                )))
                .skillList(new ArrayList<>(List.of(
                        "조각update", "조가악update"
                )))
                .build();

        //When
        ResumeException exception = assertThrows(ResumeException.class, () ->
                resumeService.modifyV2(requestDto, mockMember));

        //Then
        verify(resumeRepository, times(0)).findResumeWithCareerAndEducationAndSkill(mockMember.getId());
        assertThat(exception.getErrorCode()).isEqualTo(ResumeErrorCode.NOT_ENTERED_CAREER);
    }

}
