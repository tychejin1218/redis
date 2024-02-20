package com.example.jedisstandalong.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.jedisstandalong.dto.StoreDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.json.Path;

@Slf4j
@SpringBootTest
class StoreServiceTest {

  @Autowired
  private StoreService storeService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("saveStore_Redis JSON 저장")
  @Test
  void testSaveStore() throws Exception {

    // Given
    String key = "k1";

    List<StoreDto.Store.Book> books = new ArrayList<>();
    books.add(
        StoreDto.Store.Book.builder()
            .category("reference")
            .author("Nigel Rees")
            .title("Sayings of the Century")
            .price(8.95)
            .inStock(true)
            .sold(true)
            .build());
    books.add(
        StoreDto.Store.Book.builder()
            .category("fiction")
            .author("Evelyn Waugh")
            .title("Sword of Honour")
            .price(12.99)
            .inStock(false)
            .sold(true)
            .build());
    books.add(
        StoreDto.Store.Book.builder()
            .category("fiction")
            .author("Herman Melville")
            .title("Moby Dick")
            .isbn("0-553-21311-3")
            .price(8.99)
            .inStock(true)
            .sold(false)
            .build());
    books.add(
        StoreDto.Store.Book.builder()
            .category("fiction")
            .author("J. R. R. Tolkien")
            .title("The Lord of the Rings")
            .isbn("0-395-19395-8")
            .price(22.99)
            .inStock(true)
            .sold(false)
            .build());

    StoreDto.Store.Bicycle bicycle = StoreDto.Store.Bicycle.builder()
        .color("")
        .price(19.95)
        .inStock(true)
        .sold(false)
        .build();

    StoreDto.Store store = StoreDto.Store.builder()
        .book(books)
        .bicycle(bicycle)
        .build();

    StoreDto storeDto = StoreDto.builder()
        .store(store)
        .build();

    // When
    StoreDto savedStore = storeService.saveStore(key, storeDto);

    // Then
    log.debug("savedStore: {}", objectMapper.writeValueAsString(savedStore));
    assertFalse(ObjectUtils.isEmpty(savedStore));
  }

  @DisplayName("findStore_Redis JSON 조회 시 StoreDto로 반환")
  @Test
  void testFindStoreReturnStoreDto() throws Exception {

    // Given
    String key = "k1";

    // When
    StoreDto storeDto = storeService.findStore(key);

    // Then
    log.debug("storeDto: {}", objectMapper.writeValueAsString(storeDto));
    assertFalse(ObjectUtils.isEmpty(storeDto));
  }

  @DisplayName("findStore_Redis JSON 조회 시 Object로 반환")
  @Test
  void testFindStoreReturnObject() throws Exception {

    // Given
    String key = "k1";
    String[] strPaths = new String[24];
    // 상점에 있는 모든 책의 저자
    strPaths[0] = "$.store.book[*].author";
    // 모든 저자
    strPaths[1] = "$..author";
    // 상점의 모든 구성원
    strPaths[2] = "$.store.*";
    strPaths[3] = "$[\"store\"].*";
    // 상점에 있는 모든 것의 가격
    strPaths[4] = "$.store..price";
    // JSON 구조의 모든 재귀 멤버
    strPaths[5] = "$..*";
    // 모든 책
    strPaths[6] = "$..book[*]";
    // 첫 번째 책
    strPaths[7] = "$..book[0]";
    // 마지막 책
    strPaths[8] = "$..book[-1]";
    // 처음 두 권의 책
    strPaths[9] = "$..book[0:2]";
    // 처음 두 권의 책
    strPaths[10] = "$..book[0,1]";
    // 인덱스 0에서 3까지의 책(끝 인덱스는 포괄적이지 않음)
    strPaths[11] = "$..book[0:4]";
    // 인덱스 0, 2의 책
    strPaths[12] = "$..book[0:4:2]";
    // ISBN 번호가 있는 모든 책
    strPaths[13] = "$..book[?(@.isbn)]";
    // 모든 책이 10달러보다 저렴
    strPaths[14] = "$..book[?(@.price<10)]";
    // 가격대가 10달러에서 100달러인 모든 책
    strPaths[15] = "$..book[?(@.price>=10 && @.price<=100)]";
    // 모든 책이 매각 또는 품절(경로에 공백이 포함된 경우 따옴표로 묶어야함)
    strPaths[16] = "$..book[?(@.sold==true || @.in-stock==false)]";
    // 소설 범주의 모든 책
    strPaths[17] = "$.store.book[?(@.[\"category\"] == \"fiction\")]";
    // 비소설 범주의 모든 책
    strPaths[18] = "$.store.book[?(@.[\"category\"] != \"fiction\")]";

    Path path = new Path(strPaths[15]);

    // When
    Object object = storeService.findStore(key, path);

    // Then
    log.debug("object: {}", objectMapper.writeValueAsString(object));
    assertFalse(ObjectUtils.isEmpty(object));
  }
}
