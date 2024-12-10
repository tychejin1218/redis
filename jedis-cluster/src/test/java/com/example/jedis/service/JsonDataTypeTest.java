package com.example.jedis.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.jedis.component.RedisComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Jedis를 활용하여 Redis에 JSON Path 기능을 테스트
 * <p>
 * <a
 * href="https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/json-document-overview.html">Json
 * data type overview</a>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class JsonDataTypeTest {

  private static final String REDIS_KEY = "k1";
  private static final long TTL_SECONDS = 60 * 10;

  @Autowired
  RedisComponent redisComponent;

  @Autowired
  ObjectMapper objectMapper;

  @Order(1)
  @DisplayName("객체를 JSON 형식으로 저장하고, 키를 사용하여 객체를 조회")
  @Test
  void testSetAndGetJsonObject() throws Exception {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When
    StoreDto retrievedStore = redisComponent.getJsonObject(REDIS_KEY, StoreDto.class);
    log.debug("testSetAndGetJsonObject retrievedStore : {}",
        objectMapper.writeValueAsString(retrievedStore));

    // Then
    assertAll(
        () -> assertNotNull(retrievedStore),
        () -> assertFalse(retrievedStore.getStore().getBook().isEmpty()),
        () -> assertNotNull(retrievedStore.getStore().getBicycle())
    );
  }

  @Order(2)
  @DisplayName("키와 경로를 사용하여 JsonArray를 조회")
  @Test
  void testSetAndGetJsonArray() {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When & Then
    for (String jsonPath : initJsonPaths()) {

      JSONArray jsonArray = redisComponent.getJsonArray(REDIS_KEY, jsonPath);
      log.debug("testSetAndGetJsonArray jsonPath {} : jsonArray : {}",
          jsonPath, jsonArray.toString());

      assertNotNull(jsonArray);
    }
  }

  @Order(3)
  @DisplayName("키와 경로를 사용하여 JsonList를 조회")
  @Test
  void testSetAndGetJsonList() throws Exception {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When
    List<StoreDto> storeList = redisComponent.getJsonList(REDIS_KEY, StoreDto.class, "$");
    log.debug("storeList : {}", objectMapper.writeValueAsString(storeList));

    // Then
    assertNotNull(storeList);
  }

  private StoreDto createStoreDto() {

    List<StoreDto.Store.Book> books = List.of(
        StoreDto.Store.Book.of("reference", "Nigel Rees", "Sayings of the Century",
            null, 8.95, true, true),
        StoreDto.Store.Book.of("fiction", "Evelyn Waugh", "Sword of Honour",
            null, 12.99, false, true),
        StoreDto.Store.Book.of("fiction", "Herman Melville", "Moby Dick",
            "0-553-21311-3", 8.99, true, false),
        StoreDto.Store.Book.of("fiction", "J. R. R. Tolkien", "The Lord of the Rings",
            "0-395-19395-8", 22.99, false, false)
    );

    StoreDto.Store.Bicycle bicycle = StoreDto.Store.Bicycle.of("red", 19.95, true, false);
    return StoreDto.of(StoreDto.Store.of(books, bicycle));
  }

  private String[] initJsonPaths() {
    return new String[]{
        "$.store.book[*].author",         // store의 각 book의 author
        "$..author",                      // JSON의 전체 author
        "$.store.*",                      // store 객체의 전체 속성
        "$[\"store\"].*",                 // store 객체의 전체 속성 (다른 접근 방법)
        "$.store..price",                 // store 내 전체 속성의 price
        "$..*",                           // JSON 구조의 전체 요소
        "$..book[*]",                     // 각각의 book 객체
        "$..book[0]",                     // 첫 번째 book
        "$..book[-1]",                    // 마지막 book
        "$..book[0:2]",                   // 처음 두 권의 book
        "$..book[0,1]",                   // 첫 번째와 두 번째 book
        "$..book[0:4]",                   // 인덱스 0부터 3까지의 book
        "$..book[0:4:2]",                 // 인덱스 0과 2의 book
        "$..book[?(@.isbn)]",             // ISBN 있는 각각의 book
        "$..book[?(@.price<10)]",         // 가격이 $10 미만인 각각의 book
        "$..book[?(@[\"price\"] < 10)]",  // 가격이 $10 미만인 각각의 book (다른 접근 방법)
        "$..book[?(@.price>=10&&@.price<=100)]",  // 가격이 $10 이상 $100 이하인 각각의 book
        "$..book[?(@.sold==true||@.in-stock==false)]",  // sold가 true이거나 in-stock이 false인 각각의 book
        "$.store.book[?(@.[\"category\"] == \"fiction\")]", // fiction 카테고리의 각 book
        "$.store.book[?(@.[\"category\"] != \"fiction\")]"  // fiction 카테고리에 속하지 않는 각각의 book
    };
  }

  @Getter
  @Builder
  @AllArgsConstructor(staticName = "of")
  @NoArgsConstructor
  public static class StoreDto {

    private Store store;

    @Getter
    @Builder
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class Store {

      private List<Book> book;
      private Bicycle bicycle;

      @Getter
      @Builder
      @AllArgsConstructor(staticName = "of")
      @NoArgsConstructor
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
      @Builder
      @AllArgsConstructor(staticName = "of")
      @NoArgsConstructor
      public static class Bicycle {

        private String color;
        private Double price;
        private Boolean inStock;
        private Boolean sold;
      }
    }
  }
}
