package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(5L)
                .description("нужна дрель")
                .created(LocalDateTime.of(2030, 5, 1, 12, 0))
                .items(List.of())
                .build();

        assertThat(json.write(dto))
                .hasJsonPathNumberValue("$.id", 5)
                .hasJsonPathStringValue("$.description", "нужна дрель")
                .hasJsonPathArrayValue("$.items");
    }

    @Test
    void deserialize() throws Exception {
        String content = """
                {
                  "id": 3,
                  "description": "отвертка",
                  "created": "2030-06-01T09:00:00",
                  "items": []
                }
                """;

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getDescription()).isEqualTo("отвертка");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2030, 6, 1, 9, 0));
        assertThat(dto.getItems()).isEmpty();
    }
}