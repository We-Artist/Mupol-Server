package com.mupol.mupolserver.domain.social.google;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleProfile {
    private String id;
    private String email;
    private Boolean verified_email;
    private String picture;
}
