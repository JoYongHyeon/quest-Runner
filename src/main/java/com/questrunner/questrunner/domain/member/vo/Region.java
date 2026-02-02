package com.questrunner.questrunner.domain.member.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Region {

    SEOUL("SEOUL", "서울"),
    GYEONGGI("GYEONGGI", "경기"),
    INCHEON("INCHEON", "인천"),
    GANGWON("GANGWON", "강원"),
    DAEJEON("DAEJEON", "대전"),
    SEJONG("SEJONG", "세종"),
    CHUNGNAM("CHUNGNAM", "충남"),
    CHUNGBUK("CHUNGBUK", "충북"),
    BUSAN("BUSAN", "부산"),
    ULSAN("ULSAN", "울산"),
    GYEONGNAM("GYEONGNAM", "경남"),
    GYEONGBUK("GYEONGBUK", "경북"),
    DAEGU("DAEGU", "대구"),
    GWANGJU("GWANGJU", "광주"),
    JEONNAM("JEONNAM", "전남"),
    JEONBUK("JEONBUK", "전북"),
    JEJU("JEJU", "제주"),
    REMOTE("REMOTE", "상관없음(원격)");

    private final String code;
    private final String desc;
}
