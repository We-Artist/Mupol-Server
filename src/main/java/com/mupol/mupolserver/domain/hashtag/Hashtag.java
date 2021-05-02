package com.mupol.mupolserver.domain.hashtag;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Hashtag {

    compliment("칭찬해주세요", "compliment"),
    boast("자랑하고싶어요", "boast");

    private final String ko;
    private final String en;

}
