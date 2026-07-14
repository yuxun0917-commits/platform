package com.platform.component.tree;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 树形结构构建组件
 *
 * <p>封装通用的树形结构构建逻辑，支持任意实现了 {@link TreeNode} 接口的节点类型。
 * 采用"先分组再挂载"的 O(n) 算法，避免递归带来的性能问题。</p>
 *
 * <p><b>调用规范</b>：</p>
 * <pre>{@code
 * @Autowired
 * private TreeBuilderComponent treeBuilder;
 *
 * List<MenuVO> tree = treeBuilder.build(menuList, 0L);
 * }</pre>
 *
 * @author platform
 */
@Slf4j
@Component
public class TreeBuilderComponent {

    /**
     * 构建树形结构
     *
     * <p>从扁平列表构建树形结构，返回以指定根节点 ID 为父节点的子树列表。</p>
     *
     * @param list     扁平节点列表
     * @param rootId   根节点ID（顶级节点的 parentId 等于该值）
     * @param <T>      节点ID类型
     * @param <E>      节点类型
     * @return 树形结构列表
     */
    public <T, E extends TreeNode<T>> List<E> build(List<E> list, T rootId) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 parentId 分组
        Map<T, List<E>> parentMap = list.stream()
                .collect(Collectors.groupingBy(TreeNode::getParentId));

        // 递归挂载子节点
        parentMap.forEach((parentId, children) -> {
            // 找到每个父节点，设置其 children
            list.stream()
                    .filter(node -> Objects.equals(node.getId(), parentId))
                    .forEach(node -> node.setChildren(new ArrayList<>(children)));
        });

        // 返回根节点的直接子节点
        List<E> rootChildren = parentMap.get(rootId);
        return Objects.isNull(rootChildren) ? new ArrayList<>() : rootChildren;
    }

    /**
     * 构建树形结构（自动推断根节点）
     *
     * <p>当节点列表中存在 parentId 在列表中找不到对应 id 时，视为根节点。</p>
     *
     * @param list 扁平节点列表
     * @param <T>  节点ID类型
     * @param <E>  节点类型
     * @return 树形结构列表
     */
    public <T, E extends TreeNode<T>> List<E> buildAuto(List<E> list) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有节点 ID
        List<T> ids = list.stream().map(TreeNode::getId).collect(Collectors.toList());

        // 找出根节点（parentId 不在 ids 中的节点）
        List<E> roots = list.stream()
                .filter(node -> !ids.contains(node.getParentId()))
                .collect(Collectors.toList());

        // 按 parentId 分组
        Map<T, List<E>> parentMap = list.stream()
                .collect(Collectors.groupingBy(TreeNode::getParentId));

        // 递归挂载子节点
        parentMap.forEach((parentId, children) -> {
            list.stream()
                    .filter(node -> Objects.equals(node.getId(), parentId))
                    .forEach(node -> node.setChildren(new ArrayList<>(children)));
        });

        return roots;
    }

    /**
     * 扁平化树形结构（将树展开为列表）
     *
     * @param tree 树形结构列表
     * @param <T>  节点ID类型
     * @param <E>  节点类型
     * @return 扁平列表
     */
    public <T, E extends TreeNode<T>> List<E> flatten(List<E> tree) {
        List<E> result = new ArrayList<>();
        if (Objects.isNull(tree) || tree.isEmpty()) {
            return result;
        }
        for (E node : tree) {
            result.add(node);
            if (Objects.nonNull(node.getChildren()) && !node.getChildren().isEmpty()) {
                result.addAll(flatten((List<E>) node.getChildren()));
            }
        }
        return result;
    }
}
