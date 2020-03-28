package com.business.api.util;

/**
 * 常量库
 */
public class Constants {

    /**
     * 数据库redis hash的key
     */
    public final static String DATEBASE_KEY_INSERT = "DATEBASE_KEY_INSERT";

    /**
     * 数据库处理10s还未入库的请求
     */
    public final static long DATEBASE_SAVE_TIMEOUT = 10;

    /**
     * zookeeper锁根节点
     */
    public final static String LOCK_ROOT_PATH = "/Locks";
    public final static String LOCK_NODE_SPE = "_";
//    public final static String LOCK_NODE_NAME = "Lock_";

    public final static byte[] DATA = {1,2};
}
