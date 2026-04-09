package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostPageResponseDto {
    private List<PostListReponseDto> content;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;

    //Page<PostListResponseDto> 를 반환하면 Page의 모든 요소가 반환되므로
    //필요한것만 추출해서 반환
    public PostPageResponseDto(Page<PostListReponseDto> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.size = page.getSize();
    }
}
