package com.mupol.mupolserver.domain.instrument;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Instrument {
    // percussion
    drum("드럼","drum", ""),
    marimba("마림바","marimba", ""),
    xylophone("실로폰","xylophone", ""),

    // wind
    clarinet("클라리넷","clarinet",""),
    trumpet("트럼펫","trumpet",""),
    flute("플룻", "flute", ""),
    piccolo("피콜로","piccolo", ""),
    oboe("오보에", "oboe", ""),
    recorder("리코더", "recorder", ""),

    // string
    guitar("기타", "guitar", ""),
    acoustic_guitar("어쿠스틱 기타", "acoustic guitar", ""),
    base_guitar("베이스 기타", "base guitar", ""),
    classic_guitar("클래식 기타", "classic guitar", ""),
    electric_guitar("일렉 기타", "electric guitar", ""),
    cello("첼로", "cello",""),
    contrabass("콘트라베이스", "contrabass", ""),
    piano("피아노","piano", ""),
    ukulele("우쿨렐레", "ukulele", ""),
    viola("비올라", "viola", ""),
    violin("바이올린", "violin",""),

    // electric
    keyboard("키보드", "keyboard","")
    ;


    private final String ko;
    private final String en;
    private final String imageUrl;
}
