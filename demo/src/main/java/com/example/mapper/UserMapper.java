package com.example.mapper;

import com.example.vo.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    List<UserVO> findPage(@Param("primaryKey") String primaryKey,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    UserVO findById(@Param("primaryKey") String primaryKey, @Param("id") Object id);

    int insert(UserVO payload);

    int update(@Param("primaryKey") String primaryKey,
               @Param("id") Object id,
               @Param("payload") UserVO payload);

    int delete(@Param("primaryKey") String primaryKey, @Param("id") Object id);

    String currentSchema();

    List<ColumnMeta> findColumns(@Param("schema") String schema);

    Long lastInsertId();

    long countAll();

    record ColumnMeta(String name, boolean primaryKey, boolean autoIncrement) {
    }
}
