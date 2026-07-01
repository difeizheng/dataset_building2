package com.ctg.kbFab.storage.graph.impl;

import com.ctg.kbFab.storage.graph.GraphStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国产图库存储实现（UGE兼容层）
 * 实际生产环境需替换为真实国产图库连接
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
public class UgeGraphStore implements GraphStore {

    /** 内存节点存储（生产环境替换为图库连接） */
    private final Map<String, Map<String, Object>> nodes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> edges = new ConcurrentHashMap<>();

    @Override
    public String createNode(String label, Map<String, Object> properties) {
        String nodeId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Map<String, Object> node = new HashMap<>(properties);
        node.put("_id", nodeId);
        node.put("_label", label);
        nodes.put(nodeId, node);
        log.debug("Created graph node: {} with label: {}", nodeId, label);
        return nodeId;
    }

    @Override
    public Map<String, Object> getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public void updateNode(String nodeId, Map<String, Object> properties) {
        Map<String, Object> node = nodes.get(nodeId);
        if (node != null) {
            node.putAll(properties);
        }
    }

    @Override
    public void deleteNode(String nodeId) {
        nodes.remove(nodeId);
        edges.remove(nodeId);
        // 清理指向该节点的其他边
        edges.values().forEach(list -> list.removeIf(e -> e.contains(nodeId)));
    }

    @Override
    public String createEdge(String sourceNodeId, String targetNodeId, String edgeLabel, Map<String, Object> properties) {
        String edgeId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        edges.computeIfAbsent(sourceNodeId, k -> new ArrayList<>()).add(edgeId + ":" + targetNodeId + ":" + edgeLabel);
        log.debug("Created edge: {} -> {} ({})", sourceNodeId, targetNodeId, edgeLabel);
        return edgeId;
    }

    @Override
    public List<Map<String, Object>> getNeighbors(String nodeId, int depth) {
        List<Map<String, Object>> neighbors = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(nodeId);
        visited.add(nodeId);

        int currentDepth = 0;
        while (!queue.isEmpty() && currentDepth < depth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                List<String> edgeList = edges.getOrDefault(current, Collections.emptyList());
                for (String edge : edgeList) {
                    String[] parts = edge.split(":");
                    if (parts.length >= 2) {
                        String neighborId = parts[1];
                        if (!visited.contains(neighborId)) {
                            visited.add(neighborId);
                            Map<String, Object> neighbor = nodes.get(neighborId);
                            if (neighbor != null) {
                                neighbors.add(neighbor);
                            }
                            queue.add(neighborId);
                        }
                    }
                }
            }
            currentDepth++;
        }
        return neighbors;
    }

    @Override
    public List<Map<String, Object>> getSubGraph(String nodeId, int maxDepth, int maxNodes) {
        List<Map<String, Object>> subGraph = new ArrayList<>();
        Map<String, Object> center = nodes.get(nodeId);
        if (center != null) {
            subGraph.add(center);
        }
        List<Map<String, Object>> neighbors = getNeighbors(nodeId, maxDepth);
        for (int i = 0; i < Math.min(neighbors.size(), maxNodes - 1); i++) {
            subGraph.add(neighbors.get(i));
        }
        return subGraph;
    }

    @Override
    public List<Map<String, Object>> findPath(String sourceNodeId, String targetNodeId, int maxDepth) {
        // BFS路径查找
        List<Map<String, Object>> path = new ArrayList<>();
        Map<String, String> parentMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceNodeId);
        visited.add(sourceNodeId);

        boolean found = false;
        int depth = 0;
        while (!queue.isEmpty() && depth < maxDepth && !found) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                if (current.equals(targetNodeId)) {
                    found = true;
                    break;
                }
                List<String> edgeList = edges.getOrDefault(current, Collections.emptyList());
                for (String edge : edgeList) {
                    String[] parts = edge.split(":");
                    if (parts.length >= 2) {
                        String neighborId = parts[1];
                        if (!visited.contains(neighborId)) {
                            visited.add(neighborId);
                            parentMap.put(neighborId, current);
                            queue.add(neighborId);
                        }
                    }
                }
            }
            depth++;
        }

        if (found) {
            String current = targetNodeId;
            while (current != null) {
                Map<String, Object> node = nodes.get(current);
                if (node != null) {
                    path.add(0, node);
                }
                current = parentMap.get(current);
            }
        }
        return path;
    }
}
