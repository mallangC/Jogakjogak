package com.zb.jogakjogak.jobDescription.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.jogakjogak.jobDescription.entity.QJD;
import com.zb.jogakjogak.jobDescription.entity.QToDoList;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;

import java.util.List;
import java.util.Optional;

public class ToDoListRepositoryImpl implements ToDoListRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ToDoListRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ToDoList> findToDoListWithJdByIdAndJdId(Long toDoListId, Long jdId) {
        QToDoList toDoList = QToDoList.toDoList;
        QJD jd = QJD.jD;

        ToDoList foundToDOList = queryFactory
                .selectFrom(toDoList)
                .join(toDoList.jd, jd).fetchJoin()
                .where(toDoList.id.eq(toDoListId)
                        .and(toDoList.jd.id.eq(jdId)))
                .fetchOne();

        return Optional.ofNullable(foundToDOList);
    }

    @Override
    public List<ToDoList> findToDoListsByJdIdAndCategoryWithJd(Long jdId, ToDoListType category) {
        QToDoList toDoList = QToDoList.toDoList;
        QJD jd = QJD.jD;

        return queryFactory
                .selectFrom(toDoList)
                .join(toDoList.jd, jd).fetchJoin()
                .where(toDoList.jd.id.eq(jdId)
                        .and(toDoList.category.eq(category)))
                .fetch();
    }

    @Override
    public long countToDoListsByJdIdAndCategory(Long jdId, ToDoListType category) {
       QToDoList toDoList = QToDoList.toDoList;

       Long count = queryFactory
               .select(toDoList.count())
               .from(toDoList)
               .where(toDoList.jd.id.eq(jdId)
                       .and(toDoList.category.eq(category)))
               .fetchOne();
        return count != null ? count : 0;
    }

    @Override
    public long countDoneToDoListsByJdId(Long jdId) {
        QToDoList toDoList = QToDoList.toDoList;

        Long count = queryFactory
                .select(toDoList.count())
                .from(toDoList)
                .where(toDoList.jd.id.eq(jdId)
                        .and(toDoList.isDone.isTrue()))
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public long countUndoneToDoListsByJdId(Long jdId) {
        QToDoList toDoList = QToDoList.toDoList;

        Long count = queryFactory
                .select(toDoList.count())
                .from(toDoList)
                .where(toDoList.jd.id.eq(jdId)
                        .and(toDoList.isDone.isFalse()))
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public List<ToDoList> findAllByIdsWithJd(List<Long> ids) {
        QToDoList toDoList = QToDoList.toDoList;
        QJD jd = QJD.jD;

        return queryFactory
                .selectFrom(toDoList)
                .join(toDoList.jd, jd).fetchJoin()
                .where(toDoList.id.in(ids))
                .fetch();
    }
}
