package com.mupol.mupolserver.dto.search;

import com.mupol.mupolserver.domain.instrument.Instrument;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class SearchUserResultDto {
    private Long id;
    private String username;
    private String profileImageUrl;
    private List<Instrument> favoriteInstruments;
    private int followerNumber;
    private boolean isFollowing;
}
