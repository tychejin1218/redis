package com.example.jediscluster.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StoreDto {

  private Store store;

  @Getter
  @Setter
  @Builder
  public static class Store {

    private List<Book> book;
    private Bicycle bicycle;

    @Getter
    @Setter
    @Builder
    public static class Book {

      private String category;
      private String author;
      private String title;
      private String isbn;
      private Double price;
      private Boolean inStock;
      private Boolean sold;
    }

    @Getter
    @Setter
    @Builder
    public static class Bicycle {

      private String color;
      private Double price;
      private Boolean inStock;
      private Boolean sold;
    }
  }
}
