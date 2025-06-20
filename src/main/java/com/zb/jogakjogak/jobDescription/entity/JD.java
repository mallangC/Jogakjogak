package com.zb.jogakjogak.jobDescription.entity;

import com.zb.jogakjogak.global.BaseEntity;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Table(name = "job_description")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JD extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column
    private boolean isBookmark;

    @Column
    private String companyName;

    @Column
    private String job;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String jdUrl;

    @Column
    private String memo;

    @Column
    private boolean isAlarmOn;

    @Column
    private LocalDateTime applyAt;

    @Column(nullable = false)
    private LocalDate endedAt;

    @Builder.Default
    @OneToMany(mappedBy = "jd", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToDoList> toDoLists = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void addToDoList(ToDoList toDoList) {
        if (this.toDoLists == null) {
            this.toDoLists = new ArrayList<>();
        }
        this.toDoLists.add(toDoList);
        toDoList.setJd(this);
    }

    public void isAlarmOn(boolean isAlarmOn) {
        this.isAlarmOn = isAlarmOn;
    }

    public void updateBookmarkStatus(boolean isBookmark) {
        this.isBookmark = isBookmark;
    }

    public void markJdAsApplied() {
        this.applyAt = LocalDateTime.now();
    }
    public void unMarkJdAsApplied() {
        this.applyAt = null;
    }
}
