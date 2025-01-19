package com.westsomsom.finalproject.store.dao;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.westsomsom.finalproject.store.domain.QStore.store;

@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository{

    private final JPAQueryFactory queryFactory;

    public List<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto) {
        return queryFactory
                .select(searchResponseDtoConstructor())
                .from(store)
                .where(storeIdGt(searchRequestDto.getStoreId()),
                        storeNameContains(searchRequestDto.getStoreName()),
                        startDateGoe(searchRequestDto.getStartDate()),
                        finDateGoe(searchRequestDto.getFinDate()),
                        storeLocContains(searchRequestDto.getStoreLoc()))
                .orderBy(store.storeId.asc())
                .limit(20)
                .fetch();
    }

    public List<SearchResponseDto> searchStoreCategory(String category, Integer storeId) {
        return queryFactory
                .select(searchResponseDtoConstructor())
                .from(store)
                .where(storeIdGt(storeId),
                        storeCatagoryEq(category))
                .orderBy(store.storeId.asc())
                .limit(20)
                .fetch();
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

    private BooleanExpression storeIdGt(Integer storeId) {
        if(storeId == null) {
            return null;
        }

        return store.storeId.gt(storeId);
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
