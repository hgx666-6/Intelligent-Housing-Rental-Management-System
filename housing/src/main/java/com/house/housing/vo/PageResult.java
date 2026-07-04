package com.house.housing.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer page;
    private Integer limit;

    public Long getPages() {
        return (total + limit - 1) / limit;
    }
}