package com.poetryforum.utils;

public class RedisConstants {
    // 登录验证码
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    // 登录用户
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_POEM_TTL = 30L;
    public static final String CACHE_POEM_KEY = "cache:poem:";
    public static final String CACHE_POEM_SEARCH_KEY = "cache:poem:search:";

    public static final String LOCK_POEM_KEY = "lock:POEM:";
    public static final Long LOCK_POEM_TTL = 10L;

    public static final String LIMITED_STOCK_KEY = "limited:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
