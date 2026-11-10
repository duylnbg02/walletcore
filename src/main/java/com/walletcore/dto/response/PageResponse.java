package com.walletcore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private long total;
    private int page;
    private int size;
}