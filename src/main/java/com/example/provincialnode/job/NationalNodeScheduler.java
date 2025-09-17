package com.example.provincialnode.job;

import com.example.provincialnode.service.NationalNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 全国节点定时任务类
 * 负责定时执行全国节点相关的任务，如刷新token和更新公钥
 */
@Component
public class NationalNodeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NationalNodeScheduler.class);

    @Autowired
    private NationalNodeService nationalNodeService;

    /**
     * 每1.5小时刷新一次全国节点token
     * 使用cron表达式：0 0 0/1,30 * * ? 表示每小时30分钟时执行
     * 或者使用fixedRate=5400000表示每1.5小时执行一次
     */
    @Scheduled(fixedRate = 5400000) // 5400000毫秒 = 1.5小时
    public void scheduledRefreshToken() {
        logger.info("开始执行定时任务：刷新全国节点token");
        nationalNodeService.refreshToken();
        logger.info("定时任务执行完成：刷新全国节点token");
    }

    /**
     * 每90天更新一次全国节点公钥
     * 使用cron表达式：0 0 0 1/90 * ? 表示每90天执行一次
     * 或者使用fixedRate=7776000000表示每90天执行一次
     */
    @Scheduled(fixedRate = 7776000000L) // 7776000000毫秒 = 90天
    public void scheduledUpdatePublicKey() {
        logger.info("开始执行定时任务：更新全国节点公钥");
        nationalNodeService.fetchPublicKey();
        logger.info("定时任务执行完成：更新全国节点公钥");
    }

    /**
     * 每90天更新一次密码并刷新token
     * 使用cron表达式：0 0 0 1/90 * ? 表示每90天执行一次
     * 或者使用fixedRate=7776000000表示每90天执行一次
     */
    @Scheduled(fixedRate = 7776000000L) // 7776000000毫秒 = 90天
    public void scheduledUpdatePassword() {
        logger.info("开始执行定时任务：更新全国节点密码");
        nationalNodeService.updateUserPassword("admin", "admin", "admin");
        nationalNodeService.refreshToken();
        logger.info("定时任务执行完成：更新全国节点密码");
    }

    /**
     * 系统启动时初始化全国节点配置
     * 可以在此方法中首次获取公钥和token
     */
    // @PostConstruct
    public void init() {
        logger.info("系统启动时初始化全国节点配置");
        nationalNodeService.fetchPublicKey();
        nationalNodeService.refreshToken();
        logger.info("全国节点配置初始化完成");
    }
}