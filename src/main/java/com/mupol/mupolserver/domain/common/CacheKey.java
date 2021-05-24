package com.mupol.mupolserver.domain.common;

public class CacheKey {
    public static final int DEFAULT_EXPIRE_SEC = 60; // 1 minutes
    public static final String USER_ID = "user_id";
    public static final String USER_JWT = "user_jwt";
    public static final String USER_KEYWORD = "user_keyword";
    public static final int USER_EXPIRE_SEC = 60 * 30; // 5 minutes
    public static final String VIDEO_ID = "video_id";
    public static final String VIDEOS_KEYWORD = "videos_keyword";
    public static final String VIDEOS_USER_ID = "videos_user_id";
    public static final String MONTH_VIDEOS = "month_videos";
    public static final int VIDEO_EXPIRE_SEC = 60 * 30; // 10 minutes
    public static final String HOT_KEYWORD = "hot_keyword";
    public static final int HOT_KEYWORD_EXPIRE_SEC = 60 * 15; // 15 minutes
}