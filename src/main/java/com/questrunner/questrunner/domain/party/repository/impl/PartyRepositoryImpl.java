package com.questrunner.questrunner.domain.party.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.questrunner.questrunner.api.party.dto.req.PartySearchCondition;
import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.member.vo.Region;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.repository.PartyRepositoryCustom;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.questrunner.questrunner.domain.member.entity.QMemberEntity.memberEntity;
import static com.questrunner.questrunner.domain.party.entity.QPartyEntity.partyEntity;
import static com.questrunner.questrunner.domain.party.entity.QPartySlotEntity.partySlotEntity;

@RequiredArgsConstructor
public class PartyRepositoryImpl implements PartyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PartyEntity> searchParties(PartySearchCondition condition, Pageable pageable) {

        // 1. Content 조회
        List<PartyEntity> content = queryFactory
                .selectFrom(partyEntity)
                .join(partyEntity.leader, memberEntity).fetchJoin() // 리더 정보 함께 조회(N:1)
                .leftJoin(partyEntity.slots, partySlotEntity)       // 슬롯 정보 함께 조회(1:N)
                .where(
                        eqRegion(condition.region()),
                        eqPosition(condition.position()),
                        partyEntity.status.eq(PartyStatus.RECRUITING)
                )
                .offset(pageable.getOffset()) // 페이징
                .limit(pageable.getPageSize())
                .orderBy(partyEntity.createdAt.desc())
                .fetch();

        // 2. Count 조회 (최적화)
        Long total = queryFactory
                .select(partyEntity.countDistinct())
                .from(partyEntity)
                .leftJoin(partyEntity.slots, partySlotEntity)
                .where(
                        eqRegion(condition.region()),
                        eqPosition(condition.position()),
                        partyEntity.status.eq(PartyStatus.RECRUITING)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public Optional<PartyEntity> findByIdWithAll(Long partyId) {

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(partyEntity)
                        .join(partyEntity.leader, memberEntity).fetchJoin()       // 리더
                        .leftJoin(partyEntity.slots, partySlotEntity).fetchJoin() // 슬롯
                        .where(partyEntity.id.eq(partyId))
                        .fetchOne()
        );
    }

    // --- BooleanExpression ---
    private BooleanExpression eqRegion(Region region) {
        return region != null ? partyEntity.region.eq(region) : null;
    }

    // 파티의 슬롯 중에 해당 포지션이 하나라도 포함되어 있는지 확인
    private BooleanExpression eqPosition(Position position) {
        return position != null ? partySlotEntity.position.eq(position) : null;
    }
}
