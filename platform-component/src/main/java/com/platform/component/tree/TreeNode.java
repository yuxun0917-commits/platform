package com.platform.component.tree;

import java.util.List;

/**
 * 树形节点接口
 *
 * <p>所有需要构建树形结构的实体/VO 实现该接口，保持组件的通用性。</p>
 *
 * @param <T> 节点ID类型
 * @author platform
 */
public interface TreeNode<T> {

    /**
     * 获取节点ID
     */
    T getId();

    /**
     * 获取父节点ID
     */
    T getParentId();

    /**
     * 设置子节点列表
     */
    void setChildren(List<TreeNode<T>> children);

    /**
     * 获取子节点列表
     */
    List<TreeNode<T>> getChildren();
}
