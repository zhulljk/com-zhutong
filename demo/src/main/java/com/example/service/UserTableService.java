package com.example.service;

import com.example.mapper.UserMapper;
import com.example.vo.MetaVO;
import com.example.vo.PageVO;
import com.example.vo.UserVO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserTableService {
    private final UserMapper userMapper;
    private TableMeta tableMeta;

    public UserTableService(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.tableMeta = loadMeta();
    }

    public PageVO<UserVO> findPage(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safePageSize;
        long total = userMapper.countAll();
        List<UserVO> items = userMapper.findPage(tableMeta.primaryKey, offset, safePageSize);
        return new PageVO<>(safePage, safePageSize, total, items);
    }

    public UserVO findById(Object id) {
        return userMapper.findById(tableMeta.primaryKey, id);
    }

    public UserVO create(UserVO payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is empty");
        }
        int inserted = userMapper.insert(payload);
        if (inserted <= 0) {
            return null;
        }
        if (tableMeta.primaryKeyAutoIncrement) {
            Long key = userMapper.lastInsertId();
            if (key != null) {
                return findById(key);
            }
        }
        if (payload.getId() != null) {
            return findById(payload.getId());
        }
        return payload;
    }

    public UserVO update(Object id, UserVO payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is empty");
        }
        userMapper.update(tableMeta.primaryKey, id, payload);
        return findById(id);
    }

    public int delete(Object id) {
        return userMapper.delete(tableMeta.primaryKey, id);
    }

    public MetaVO getMeta() {
        List<MetaVO.ColumnVO> columns = tableMeta.columns.stream()
                .map(column -> new MetaVO.ColumnVO(column.name(), column.primaryKey(), column.autoIncrement()))
                .collect(Collectors.toList());
        return new MetaVO(tableMeta.primaryKey, columns);
    }

    private TableMeta loadMeta() {
        String schema = userMapper.currentSchema();
        if (!StringUtils.hasText(schema)) {
            throw new IllegalStateException("database not selected");
        }
        List<UserMapper.ColumnMeta> columns = userMapper.findColumns(schema);
        if (columns.isEmpty()) {
            throw new IllegalStateException("user table not found");
        }
        String primaryKey = columns.stream()
                .filter(UserMapper.ColumnMeta::primaryKey)
                .map(UserMapper.ColumnMeta::name)
                .findFirst()
                .orElse(columns.get(0).name());
        boolean primaryKeyAutoIncrement = columns.stream()
                .filter(col -> col.name().equals(primaryKey))
                .findFirst()
                .map(UserMapper.ColumnMeta::autoIncrement)
                .orElse(false);
        return new TableMeta(primaryKey, primaryKeyAutoIncrement, columns);
    }

    public record TableMeta(String primaryKey,
                            boolean primaryKeyAutoIncrement,
                            List<UserMapper.ColumnMeta> columns) {
    }
}
