package com.example.cs6200_hw2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MedicineTests {

    private final String json = "{" +
            "\"description\":\"This is a wonderful description\"" +
            "}";

    private final ObjectMapper mapper = MedicineServiceImpl.createMapper();

    @Test
    public void testObjectMapperToMedicine() throws Exception {
        Medicine medicine = mapper.readValue(json, Medicine.class);
        assertThat(medicine.getId()).isNull();
        assertThat(medicine.getDescription()).isEqualTo("This is a wonderful description");

        // now vice versa. serialize out again
        final String serializedJson = mapper.writeValueAsString(medicine);
        assertThat(serializedJson).isEqualTo(json);
    }
}
