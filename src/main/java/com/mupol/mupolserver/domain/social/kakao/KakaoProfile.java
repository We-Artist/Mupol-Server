package com.mupol.mupolserver.domain.social.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoProfile {

    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    @Setter
    @ToString
     public class KakaoAccount {
        private Profile profile;
        private String email;
    }

    @Getter
    @Setter
    @ToString
    public class Profile {
        private String nickname;
        private String profile_image_url;
    }
}
