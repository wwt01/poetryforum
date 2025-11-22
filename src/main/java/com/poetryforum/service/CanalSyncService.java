package com.poetryforum.service;

import org.springframework.context.SmartLifecycle;

public interface CanalSyncService extends SmartLifecycle {
    // 继承SmartLifecycle接口，Spring会自动调用start()方法
}