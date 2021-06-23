package com.mupol.mupolserver.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
    addInstrument("악기 추가"),
    bug("버그 있어요!"),
    improvement("개선 요청"),
    etc("기타")
    ;

    private String ko;
}
