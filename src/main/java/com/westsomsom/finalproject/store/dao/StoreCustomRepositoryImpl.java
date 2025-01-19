package com.westsomsom.finalproject.store.dao;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.westsomsom.finalproject.store.domain.QStore;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.westsomsom.finalproject.store.domain.QStore.store;

@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository{

    private final JPAQueryFactory queryFactory;

//    public Page<Store> getAllStores(Pageable pageable) {
//        List<Store> storeList = queryFactory
//                .selectFrom(store)
//                .limit(pageable.getPageSize())
//                .offset(pageable.getOffset())
//                .fetch();
//
//        JPAQuery<Long> countQuery = queryFactory
//                .select(store.count())
//                .from(store);
//
//        return PageableExecutionUtils.getPage(storeList, pageable, countQuery::fetchOne);
//    }

    @Override
    public Slice<Store> findStoresNoOffset(Integer lastStoreId, Pageable pageable) {
        QStore store = QStore.store;

        List<Store> stores = queryFactory
                .selectFrom(store)
                .where(lastStoreId != null ? store.storeId.lt(lastStoreId) : null)
                .orderBy(store.storeId.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = stores.size() > pageable.getPageSize();
        if (hasNext) {
            stores.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(stores, pageable, hasNext);
    }

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

    private ConstructorExpression<SearchResponseDto> searchResponseDtoConstructor() {
        return Projections.constructor(SearchResponseDto.class,
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
