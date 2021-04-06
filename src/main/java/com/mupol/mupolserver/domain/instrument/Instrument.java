package com.mupol.mupolserver.domain.instrument;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Instrument {
    piano("피아노","piano"),
    piccolo("피콜로","piccolo"),
    drum("드럼","drum"),
    guitar("기타", "guitar")
    ;

    private final String ko;
    private final String en;
}
