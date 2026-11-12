package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    void deserialize() throws Exception {
        String content = "{\"itemId\": 1, \"start\": \"2030-01-01T10:00:00\", \"end\": \"2030-01-02T12:00:00\"}";

        BookItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 12, 0));
    }

    @Test
    void serialize() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2030, 1, 1, 10, 0));
        dto.setEnd(LocalDateTime.of(2030, 1, 2, 12, 0));

        assertThat(json.write(dto))
                .hasJsonPathNumberValue("$.itemId")
                .hasJsonPath("$.start")
                .hasJsonPath("$.end");
    }
}