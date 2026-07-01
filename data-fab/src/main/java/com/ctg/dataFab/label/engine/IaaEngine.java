package com.ctg.dataFab.label.engine;

/**
 * IAA (Inter-Annotator Agreement) 计算引擎
 * 实现 Cohen's Kappa 和 Fleiss' Kappa 算法
 *
 * M3 标注一致性要求：
 * - Kappa >= 0.85 → 双标签采纳
 * - 0.70 <= Kappa < 0.85 → 进入仲裁池
 * - Kappa < 0.70 → 自动触发重标
 *
 * @author Developer
 * @since 2026-07-01
 */
public class IaaEngine {

    /** 采纳阈值 */
    public static final double THRESHOLD_ACCEPT = 0.85;
    /** 仲裁阈值 */
    public static final double THRESHOLD_ARBITRATE = 0.70;

    /**
     * 计算 Cohen's Kappa（两名标注员）
     *
     * @param labels1 标注员1的标签数组
     * @param labels2 标注员2的标签数组
     * @return Kappa值 [-1, 1]
     */
    public static double calculateCohenKappa(String[] labels1, String[] labels2) {
        if (labels1.length != labels2.length) {
            throw new IllegalArgumentException("两组标注数量不一致");
        }

        int n = labels1.length;
        if (n == 0) {
            return 0.0;
        }

        // 计算观察一致性 Po
        long agreements = 0;
        for (int i = 0; i < n; i++) {
            if (labels1[i].equals(labels2[i])) {
                agreements++;
            }
        }
        double po = (double) agreements / n;

        // 计算期望一致性 Pe
        // 统计每个类别的频次
        java.util.Map<String, Integer> count1 = new java.util.HashMap<>();
        java.util.Map<String, Integer> count2 = new java.util.HashMap<>();

        for (String label : labels1) {
            count1.merge(label, 1, Integer::sum);
        }
        for (String label : labels2) {
            count2.merge(label, 1, Integer::sum);
        }

        double pe = 0.0;
        java.util.Set<String> allLabels = new java.util.HashSet<>(count1.keySet());
        allLabels.addAll(count2.keySet());

        for (String label : allLabels) {
            double p1 = (double) count1.getOrDefault(label, 0) / n;
            double p2 = (double) count2.getOrDefault(label, 0) / n;
            pe += p1 * p2;
        }

        // Kappa = (Po - Pe) / (1 - Pe)
        if (pe == 1.0) {
            return 1.0;
        }
        return (po - pe) / (1.0 - pe);
    }

    /**
     * 计算 Fleiss' Kappa（多名标注员）
     *
     * @param annotations 每个样本的标注矩阵 [样本数][标注员数]
     * @param categories 类别数
     * @return Kappa值
     */
    public static double calculateFleissKappa(int[][] annotations, int categories) {
        int n = annotations.length;    // 样本数
        int k = categories;            // 类别数
        int m = annotations[0].length;  // 标注员数

        if (n == 0 || k == 0 || m == 0) {
            return 0.0;
        }

        // 计算每个样本的一致性 Pi
        double[] piValues = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < k; j++) {
                sum += annotations[i][j] * annotations[i][j];
            }
            piValues[i] = (sum - m) / (m * (m - 1));
        }

        // 平均一致性 P_bar
        double pBar = 0.0;
        for (double pi : piValues) {
            pBar += pi;
        }
        pBar /= n;

        // 计算每个类别的边际概率 Pj
        double[] pjValues = new double[k];
        for (int j = 0; j < k; j++) {
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += annotations[i][j];
            }
            pjValues[j] = sum / (n * m);
        }

        // 期望一致性 Pe
        double pe = 0.0;
        for (double pj : pjValues) {
            pe += pj * pj;
        }

        if (pe == 1.0) {
            return 1.0;
        }

        return (pBar - pe) / (1.0 - pe);
    }

    /**
     * 判断标注结果处理方式
     *
     * @param kappa Kappa值
     * @return ACCEPT / ARBITRATE / RELABEL
     */
    public static IaaDecision decide(double kappa) {
        if (kappa >= THRESHOLD_ACCEPT) {
            return IaaDecision.ACCEPT;
        } else if (kappa >= THRESHOLD_ARBITRATE) {
            return IaaDecision.ARBITRATE;
        } else {
            return IaaDecision.RELABEL;
        }
    }

    /**
     * IAA决策枚举
     */
    public enum IaaDecision {
        /** 采纳 */
        ACCEPT,
        /** 进入仲裁 */
        ARBITRATE,
        /** 触发重标 */
        RELABEL
    }
}
