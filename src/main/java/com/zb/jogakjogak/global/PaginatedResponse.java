package com.zb.jogakjogak.global;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
  private List<T> content;
  private long totalElements;
  private int totalPages;
  private int number;
  private int size;
  private String message;
  private HttpStatus status;

  public static <T> PaginatedResponse<T> from(Page<T> page, String message, HttpStatus status) {
    return PaginatedResponse.<T>builder()
            .content(page.getContent())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .number(page.getNumber())
            .size(page.getSize())
            .message(message)
            .status(status)
            .build();
  }

}
