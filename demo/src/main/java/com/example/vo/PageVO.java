package com.example.vo;

import java.util.List;

public record PageVO<T>(int page, int pageSize, long total, List<T> items) {
}
