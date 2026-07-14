package com.platform.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.Objects;

/**
 * 分页对象
 * @param <T>
 */
@JsonIgnoreProperties({"size", "current", "pages"})
public class Paging<T> extends Page<T> {

    /**
     * 当前页
     */
    @Getter
    private Integer page;

    /**
     * 页大小
     */
    @Getter
    private Integer pageSize;


    public Paging(Integer page, Integer pageSize) {
        setPage(page);
        setPageSize(pageSize);
    }

    public void setPage(Integer page) {
        if (Objects.isNull(page) || page <= 0) {
            this.page = 1;
            return;
        }
        this.page = page;
        setCurrent(page);
    }

    public void setPageSize(Integer pageSize) {
        if (Objects.isNull(pageSize) || pageSize <= 0) {
            this.pageSize = 20;
            return;
        }
        this.pageSize = pageSize;
        setSize(pageSize);
    }
}
