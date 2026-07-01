package com.ctg.kbFab.storage.graph;

import java.util.List;
import java.util.Map;

/**
 * 图库存储接口（国产图库抽象层）
 * 支持 UGE / GeaGraph / TuGraph 等国产图库
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface GraphStore {

    /**
     * 创建节点
     */
    String createNode(String label, Map<String, Object> properties);

    /**
     * 查询节点
     */
    Map<String, Object> getNode(String nodeId);

    /**
     * 更新节点属性
     */
    void updateNode(String nodeId, Map<String, Object> properties);

    /**
     * 删除节点
     */
    void deleteNode(String nodeId);

    /**
     * 创建边/关系
     */
    String createEdge(String sourceNodeId, String targetNodeId, String edgeLabel, Map<String, Object> properties);

    /**
     * 查询邻居节点
     */
    List<Map<String, Object>> getNeighbors(String nodeId, int depth);

    /**
     * 子图查询
     */
    List<Map<String, Object>> getSubGraph(String nodeId, int maxDepth, int maxNodes);

    /**
     * 路径查询
     */
    List<Map<String, Object>> findPath(String sourceNodeId, String targetNodeId, int maxDepth);
}
