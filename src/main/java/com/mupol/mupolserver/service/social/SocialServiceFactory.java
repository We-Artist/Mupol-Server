package com.mupol.mupolserver.service.social;

import com.mupol.mupolserver.domain.user.SnsType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SocialServiceFactory {
    private final Map<SnsType, SocialService> socialServices = new HashMap<>();

    public SocialServiceFactory(List<SocialService> socialServices) {
        if(CollectionUtils.isEmpty(socialServices)) {
            throw new IllegalArgumentException("not exist social service");
        }

        for (SocialService socialService : socialServices) {
            this.socialServices.put(socialService.getSnsType(), socialService);
        }
    }

    public SocialService getService(SnsType snsType) {
        return socialServices.get(snsType);
    }
}
