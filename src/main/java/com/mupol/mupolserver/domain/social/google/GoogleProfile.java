package com.mupol.mupolserver.domain.social.google;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleProfile {
    private String id;
    private Boolean verified_email;
    private String email;
    private String picture;
}
