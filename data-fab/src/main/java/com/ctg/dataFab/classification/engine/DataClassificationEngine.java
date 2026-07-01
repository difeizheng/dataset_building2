package com.ctg.dataFab.classification.engine;

import com.ctg.dataFab.common.enums.DataLevel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 数据分级引擎 (M1)
 * 实现 L1-L4 四级自动分级
 *
 * L1 公开：可对外发布的数据
 * L2 内部：集团内部一般业务数据
 * L3 敏感：内部重要数据，需要TDE+字段掩码+部门审批
 * L4 核心：集团核心竞争优势、国家安全相关数据，需要国密SM4加密+多重审批+阻断式审计+禁下载/打印/外发
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
public class DataClassificationEngine {

    /**
     * 分级结果
     */
    @Data
    public static class ClassificationResult {
        private DataLevel level;
        private String reason;
        private List<String> matchedRules;
        private boolean requiresEncryption;
        private boolean requiresApproval;
        private boolean blockDownload;
        private boolean blockPrint;
        private boolean blockExport;
        private boolean requireWatermark;

        public ClassificationResult() {
            this.matchedRules = new ArrayList<>();
        }
    }

    /**
     * 分级规则配置
     */
    private static final Map<DataLevel, ClassificationRule> RULES = new LinkedHashMap<>();

    static {
        // L4 核心数据规则
        ClassificationRule l4Rule = new ClassificationRule();
        l4Rule.setKeywords(Arrays.asList(
                "核心机密", "国家安全", "战略", "绝密", "核心算法",
                "核心模型", "核心参数", "商业机密", "核心专利", "国防"
        ));
        l4Rule.setPatterns(Arrays.asList(
                Pattern.compile(".*核心.*机密.*"),
                Pattern.compile(".*国家安全.*"),
                Pattern.compile(".*绝密.*"),
                Pattern.compile(".*战略.*决策.*")
        ));
        l4Rule.setFileTypes(Arrays.asList("encrypted", "secure"));
        RULES.put(DataLevel.L4_CORE, l4Rule);

        // L3 敏感数据规则
        ClassificationRule l3Rule = new ClassificationRule();
        l3Rule.setKeywords(Arrays.asList(
                "敏感", "机密", "内部重要", "财务", "人事",
                "薪资", "合同", "客户信息", "身份证", "手机号"
        ));
        l3Rule.setPatterns(Arrays.asList(
                Pattern.compile(".*敏感.*数据.*"),
                Pattern.compile(".*财务.*报表.*"),
                Pattern.compile(".*人事.*档案.*"),
                Pattern.compile(".*客户.*信息.*")
        ));
        l3Rule.setFileTypes(Arrays.asList("confidential"));
        RULES.put(DataLevel.L3_SENSITIVE, l3Rule);

        // L2 内部数据规则
        ClassificationRule l2Rule = new ClassificationRule();
        l2Rule.setKeywords(Arrays.asList(
                "内部", "一般业务", "工作文档", "会议纪要",
                "内部通知", "内部报告"
        ));
        l2Rule.setPatterns(Arrays.asList(
                Pattern.compile(".*内部.*"),
                Pattern.compile(".*工作.*文档.*")
        ));
        l2Rule.setFileTypes(Arrays.asList("internal"));
        RULES.put(DataLevel.L2_INTERNAL, l2Rule);

        // L1 公开数据规则 (默认)
        ClassificationRule l1Rule = new ClassificationRule();
        l1Rule.setKeywords(Arrays.asList(
                "公开", "对外", "发布", "公告"
        ));
        l1Rule.setPatterns(Arrays.asList(
                Pattern.compile(".*公开.*"),
                Pattern.compile(".*对外.*发布.*")
        ));
        l1Rule.setFileTypes(Arrays.asList("public"));
        RULES.put(DataLevel.L1_PUBLIC, l1Rule);
    }

    /**
     * 自动分级
     *
     * @param context 分级上下文
     * @return 分级结果
     */
    public static ClassificationResult classify(ClassificationContext context) {
        log.info("开始数据分级: name={}, type={}", context.getName(), context.getType());

        ClassificationResult result = new ClassificationResult();

        // 按优先级检查每个级别
        for (Map.Entry<DataLevel, ClassificationRule> entry : RULES.entrySet()) {
            DataLevel level = entry.getKey();
            ClassificationRule rule = entry.getValue();

            if (matchesRule(context, rule)) {
                result.setLevel(level);
                result.getMatchedRules().add("匹配规则: " + level.name());
                break;
            }
        }

        // 如果没有匹配任何规则，默认为L1
        if (result.getLevel() == null) {
            result.setLevel(DataLevel.L1_PUBLIC);
            result.getMatchedRules().add("默认分级: L1_PUBLIC");
        }

        // 设置安全要求
        applySecurityRequirements(result);

        // 生成分级原因
        result.setReason(generateReason(result));

        log.info("数据分级完成: level={}, reason={}", result.getLevel(), result.getReason());
        return result;
    }

    /**
     * 检查是否匹配规则
     */
    private static boolean matchesRule(ClassificationContext context, ClassificationRule rule) {
        // 检查关键词
        String text = (context.getName() + " " +
                (context.getDescription() != null ? context.getDescription() : "") + " " +
                (context.getTags() != null ? context.getTags() : "")).toLowerCase();

        for (String keyword : rule.getKeywords()) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        // 检查正则模式
        for (Pattern pattern : rule.getPatterns()) {
            if (pattern.matcher(text).matches()) {
                return true;
            }
        }

        // 检查文件类型
        if (context.getMetadata() != null) {
            for (String fileType : rule.getFileTypes()) {
                if (context.getMetadata().toLowerCase().contains(fileType.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 应用安全要求
     */
    private static void applySecurityRequirements(ClassificationResult result) {
        switch (result.getLevel()) {
            case L4_CORE:
                result.setRequiresEncryption(true);  // 国密SM4加密
                result.setRequiresApproval(true);     // 多重审批
                result.setBlockDownload(true);        // 禁止下载
                result.setBlockPrint(true);           // 禁止打印
                result.setBlockExport(true);          // 禁止外发
                result.setRequireWatermark(true);     // 强制水印
                break;
            case L3_SENSITIVE:
                result.setRequiresEncryption(true);   // TDE加密
                result.setRequiresApproval(true);     // 部门审批
                result.setBlockDownload(false);
                result.setBlockPrint(false);
                result.setBlockExport(false);
                result.setRequireWatermark(true);     // 建议水印
                break;
            case L2_INTERNAL:
                result.setRequiresEncryption(false);
                result.setRequiresApproval(false);
                result.setBlockDownload(false);
                result.setBlockPrint(false);
                result.setBlockExport(false);
                result.setRequireWatermark(false);
                break;
            case L1_PUBLIC:
            default:
                result.setRequiresEncryption(false);
                result.setRequiresApproval(false);
                result.setBlockDownload(false);
                result.setBlockPrint(false);
                result.setBlockExport(false);
                result.setRequireWatermark(false);
                break;
        }
    }

    /**
     * 生成分级原因
     */
    private static String generateReason(ClassificationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("数据分级为 ").append(result.getLevel().getName());
        sb.append(" (").append(result.getLevel().getDescription()).append(")");

        if (!result.getMatchedRules().isEmpty()) {
            sb.append("。匹配规则: ").append(String.join(", ", result.getMatchedRules()));
        }

        if (result.isRequiresEncryption()) {
            sb.append("。需要加密保护");
        }
        if (result.isBlockDownload()) {
            sb.append("。禁止下载");
        }
        if (result.isBlockPrint()) {
            sb.append("。禁止打印");
        }
        if (result.isBlockExport()) {
            sb.append("。禁止外发");
        }

        return sb.toString();
    }

    /**
     * 分级规则内部类
     */
    @Data
    private static class ClassificationRule {
        private List<String> keywords = new ArrayList<>();
        private List<Pattern> patterns = new ArrayList<>();
        private List<String> fileTypes = new ArrayList<>();
    }

    /**
     * 分级上下文
     */
    @Data
    public static class ClassificationContext {
        private String name;
        private String description;
        private String type;
        private String tags;
        private String metadata;

        public ClassificationContext() {
        }

        public ClassificationContext(String name, String description, String type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
    }
}
