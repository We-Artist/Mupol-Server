package com.mupol.mupolserver.dto.user;

import com.mupol.mupolserver.domain.instrument.Instrument;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class UserResDto {
    private Long id;
    private String username;
    private String bgImageUrl;
    private String bio;
    private String email;
    private List<Instrument> favoriteInstrumentList;
    private boolean major;
    private String profileImageUrl;
    private Long createdAt;
    private Long representativeVideoId;
    private Integer followingCount;
    private Integer followerCount;
    private Integer videoCount;
}
