package com.zhut.menu.util;

/**
 * 雪花算法 ID 生成器
 * 基于 Twitter 的 Snowflake 算法实现
 */
public class SnowflakeIdGenerator {
    
    // 起始时间戳（2024-01-01 00:00:00 UTC）
    private static final long START_TIMESTAMP = 1704067200000L;
    
    // 机器 ID 位数
    private static final long MACHINE_BITS = 5L;
    // 数据中心 ID 位数
    private static final long DATA_CENTER_BITS = 5L;
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;
    
    // 机器 ID 最大值
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_BITS);
    // 数据中心 ID 最大值
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_BITS);
    // 序列号最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    // 机器 ID 左移位数
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    // 数据中心 ID 左移位数
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    // 时间戳左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATA_CENTER_BITS;
    
    private final long machineId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    private static volatile SnowflakeIdGenerator instance;
    
    private SnowflakeIdGenerator(long machineId, long dataCenterId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data Center ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }
        this.machineId = machineId;
        this.dataCenterId = dataCenterId;
    }
    
    /**
     * 获取单例实例（默认机器 ID 和数据中心 ID 为 1）
     */
    public static SnowflakeIdGenerator getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(1L, 1L);
                }
            }
        }
        return instance;
    }
    
    /**
     * 生成下一个 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID for " 
                    + (lastTimestamp - timestamp) + " milliseconds");
        }
        
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }
    
    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * 生成下一个 ID（静态方法，使用默认实例）
     */
    public static long generate() {
        return getInstance().nextId();
    }
}
