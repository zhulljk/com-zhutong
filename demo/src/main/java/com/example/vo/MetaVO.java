package com.example.vo;

import java.util.List;

public record MetaVO(String primaryKey, List<ColumnVO> columns) {
    public record ColumnVO(String name, boolean primaryKey, boolean autoIncrement) {
    }
}
