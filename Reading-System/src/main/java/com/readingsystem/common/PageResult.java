package com.readingsystem.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private Long total;
    private Long current;
    private Long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords());
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }

    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> mapper) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords() == null ? Collections.emptyList()
                : page.getRecords().stream().map(mapper).toList());
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }

    public static <T> PageResult<T> empty(long current, long size) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(Collections.emptyList());
        r.setTotal(0L);
        r.setCurrent(current);
        r.setSize(size);
        return r;
    }
}
