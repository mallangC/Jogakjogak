package com.zb.jogakjogak.jobDescription.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_descriptions")
@Getter
@NoArgsConstructor
public class JD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = true)
    private String jdUrl;

    @Column(nullable = true)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "jd", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToDoList> toDoLists = new ArrayList<>();

    @Builder
    private JD(String title, String jdUrl, String memo, LocalDateTime endedAt, List<ToDoList> toDoLists) {
        this.title = title;
        this.jdUrl = jdUrl;
        this.memo = memo;
        this.endedAt = endedAt;
        this.toDoLists = (toDoLists != null) ? toDoLists : new ArrayList<>();
    }

    public void addToDoList(ToDoList toDoList) {
        if (this.toDoLists == null) {
            this.toDoLists = new ArrayList<>();
        }
        this.toDoLists.add(toDoList);
        toDoList.setJd(this);
    }

    public void removeToDoList(ToDoList toDoList) {
        if (this.toDoLists != null) {
            this.toDoLists.remove(toDoList);
            toDoList.setJd(null);
        }
    }
}
