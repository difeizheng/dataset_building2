package com.ctg.dataFab.label.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * IAA引擎单元测试
 * 覆盖 M3 标注一致性检验机制
 *
 * @author Developer
 * @since 2026-07-01
 */
class IaaEngineTest {

    @Test
    void testCohenKappa_perfectAgreement() {
        String[] labels1 = {"A", "B", "C", "A", "B"};
        String[] labels2 = {"A", "B", "C", "A", "B"};

        double kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
        assertEquals(1.0, kappa, 0.001, "完全一致时Kappa应为1.0");
    }

    @Test
    void testCohenKappa_noAgreement() {
        String[] labels1 = {"A", "A", "A", "A", "A"};
        String[] labels2 = {"B", "B", "B", "B", "B"};

        double kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
        assertTrue(kappa <= 0.0, "完全不一致时Kappa应<=0");
    }

    @Test
    void testCohenKappa_partialAgreement() {
        String[] labels1 = {"A", "B", "A", "B", "A", "B", "A", "B", "A", "B"};
        String[] labels2 = {"A", "B", "A", "B", "A", "A", "B", "B", "A", "A"};

        double kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
        assertTrue(kappa > 0.0 && kappa < 1.0, "部分一致时Kappa应在0-1之间");
    }

    @Test
    void testCohenKappa_emptyArrays() {
        String[] labels1 = {};
        String[] labels2 = {};

        double kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
        assertEquals(0.0, kappa, 0.001, "空数组时Kappa应为0");
    }

    @Test
    void testCohenKappa_mismatchedLengths() {
        String[] labels1 = {"A", "B"};
        String[] labels2 = {"A", "B", "C"};

        assertThrows(IllegalArgumentException.class,
                () -> IaaEngine.calculateCohenKappa(labels1, labels2),
                "数组长度不一致应抛出异常");
    }

    @Test
    void testFleissKappa_perfectAgreement() {
        // 3个样本，3个类别，3个标注员
        // 每个样本所有标注员都一致
        int[][] annotations = {
            {3, 0, 0},  // 样本1：全部标注为类别0
            {0, 3, 0},  // 样本2：全部标注为类别1
            {0, 0, 3}   // 样本3：全部标注为类别2
        };

        double kappa = IaaEngine.calculateFleissKappa(annotations, 3);
        assertEquals(1.0, kappa, 0.001, "完全一致时Fleiss Kappa应为1.0");
    }

    @Test
    void testDecide_accept() {
        assertEquals(IaaEngine.IaaDecision.ACCEPT,
                IaaEngine.decide(0.90), "Kappa>=0.85应采纳");
        assertEquals(IaaEngine.IaaDecision.ACCEPT,
                IaaEngine.decide(0.85), "Kappa=0.85应采纳");
    }

    @Test
    void testDecide_arbitrate() {
        assertEquals(IaaEngine.IaaDecision.ARBITRATE,
                IaaEngine.decide(0.75), "0.70<=Kappa<0.85应仲裁");
        assertEquals(IaaEngine.IaaDecision.ARBITRATE,
                IaaEngine.decide(0.70), "Kappa=0.70应仲裁");
    }

    @Test
    void testDecide_relabel() {
        assertEquals(IaaEngine.IaaDecision.RELABEL,
                IaaEngine.decide(0.50), "Kappa<0.70应重标");
        assertEquals(IaaEngine.IaaDecision.RELABEL,
                IaaEngine.decide(0.0), "Kappa=0应重标");
        assertEquals(IaaEngine.IaaDecision.RELABEL,
                IaaEngine.decide(-0.5), "Kappa<0应重标");
    }

    @Test
    void testThresholds() {
        assertEquals(0.85, IaaEngine.THRESHOLD_ACCEPT, 0.001);
        assertEquals(0.70, IaaEngine.THRESHOLD_ARBITRATE, 0.001);
    }
}
