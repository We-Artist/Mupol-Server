package com.mupol.mupolserver.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
    addInstrument("악기 추가", 0),
    bug("버그 있어요!", 1),
    improvement("개선 요청", 2),
    etc("기타", 3);

    private String ko;
    private Integer id;
}
