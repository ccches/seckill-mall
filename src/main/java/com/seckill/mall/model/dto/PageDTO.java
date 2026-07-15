package com.seckill.mall.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页返回对象——替代 MyBatis-Plus 的 Page，避免分页插件依赖问题。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    private long total;
    private int page;
    private int size;
    private List<T> records;

    public static <T> PageDTO<T> of(long total, int page, int size, List<T> records) {
        return new PageDTO<>(total, page, size, records);
    }
}
