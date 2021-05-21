package com.mupol.mupolserver.service.social;

import com.mupol.mupolserver.domain.user.SnsType;

public interface SocialService {
    public String getSnsId(String token);
    public String getProfileImageUrl(String token);
    public String getEmail(String token);
    public SnsType getSnsType();
}
