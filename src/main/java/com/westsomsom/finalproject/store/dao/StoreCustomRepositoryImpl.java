package com.westsomsom.finalproject.store.dao;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.westsomsom.finalproject.store.domain.QStore.store;

@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository{

    private final JPAQueryFactory queryFactory;

    public Page<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto, Pageable pageable) {
        List<SearchResponseDto> storeList = queryFactory
                .select(searchResponseDtoConstructor())
                .from(store)
                .where(storeNameContains(searchRequestDto.getStoreName()),
                        startDateGoe(searchRequestDto.getStartDate()),
                        finDateGoe(searchRequestDto.getFinDate()),
                        storeLocContains(searchRequestDto.getStoreLoc()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(store.count())
                .from(store)
                .where(storeNameContains(searchRequestDto.getStoreName()),
                        startDateGoe(searchRequestDto.getStartDate()),
                        finDateGoe(searchRequestDto.getFinDate()),
                        storeLocContains(searchRequestDto.getStoreLoc())
                );

        return PageableExecutionUtils.getPage(storeList, pageable, countQuery::fetchOne);
    }

    public Page<SearchResponseDto> searchStoreCategory(String category, Pageable pageable) {
        List<SearchResponseDto> storeList = queryFactory
                .select(searchResponseDtoConstructor())
                .from(store)
                .where(storeCatagoryEq(category))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(store.count())
                .from(store)
                .where(storeCatagoryEq(category));

        return PageableExecutionUtils.getPage(storeList, pageable, countQuery::fetchOne);
    }

    private QBean<SearchResponseDto> searchResponseDtoConstructor() {
        return Projections.fields(SearchResponseDto.class,
                store.storeName,
                store.storeBio,
                store.startDate,
                store.finDate,
                store.storeCategory,
                store.storeLoc
        );
    }

    private BooleanExpression storeNameContains(String storeName) {
        return storeName != null ? store.storeName.contains(storeName) : null;
    }

    private BooleanExpression startDateGoe(LocalDate startDate) {
        return startDate != null ? store.startDate.goe(startDate) : null;
    }

    private BooleanExpression finDateGoe(LocalDate finDate) {
        return finDate != null ? store.finDate.loe(finDate) : null;
    }

    private BooleanExpression storeLocContains(String storeLoc) {
        return storeLoc != null ? store.storeLoc.contains(storeLoc) : null;
    }

    private BooleanExpression storeCatagoryEq(String storeCategory) {
        return storeCategory != null ? store.storeCategory.eq(storeCategory) : null;
    }
}
