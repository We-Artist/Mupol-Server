package com.mupol.mupolserver.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportVideoType {

    slander("욕설, 비방, 차별, 혐오"),
    commercial("홍보, 영리목적"),
    illegal("불법 정보"),
    obscene("음란, 청소년 유해"),
    personal("개인 정보 노출, 유포, 거래"),
    spam("도배 및 스팸"),
    etc("기타")
    ;

    private String ko;
}
