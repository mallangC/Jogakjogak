package com.zb.jogakjogak.jobDescription.entity;

import com.zb.jogakjogak.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_descriptions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JD extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String companyName;

    @Column
    private String job;

    @Column
    private String content;

    @Column(nullable = true)
    private String jdUrl;

    @Column(nullable = true)
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
}
