package com.readingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.readingsystem.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
