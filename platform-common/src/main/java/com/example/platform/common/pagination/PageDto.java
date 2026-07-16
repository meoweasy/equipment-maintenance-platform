package com.example.platform.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageDto<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;

    public static <T> PageDto<T> of(Page<T> page) {
        return new PageDto<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }

    public static <T, U> PageDto<U> of(
            Page<T> page,
            Function<T, U> converter
    ) {
        return new PageDto<>(
                page.getContent()
                        .stream()
                        .map(converter)
                        .toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }

    public PageDto(List<T> content) {
        this.content = content;
        this.totalElements = content.size();
        this.totalPages = 1;
        this.pageSize = content.size();
        this.pageNumber = 0;
    }
}
