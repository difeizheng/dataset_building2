package com.ctg.integration.dto.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统资源视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemResourceVO {

    private CpuInfo cpu;
    private MemoryInfo memory;
    private DiskInfo disk;
    private NetworkInfo network;
    private JvmInfo jvm;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CpuInfo {
        private Double usagePercent;
        private Integer cores;
        private Double loadAverage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {
        private Long totalBytes;
        private Long usedBytes;
        private Long freeBytes;
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskInfo {
        private Long totalBytes;
        private Long usedBytes;
        private Long freeBytes;
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkInfo {
        private Long bytesSent;
        private Long bytesReceived;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JvmInfo {
        private Long heapUsed;
        private Long heapMax;
        private Long nonHeapUsed;
        private Integer threadCount;
        private Integer daemonThreadCount;
        private Long uptime;
    }
}
