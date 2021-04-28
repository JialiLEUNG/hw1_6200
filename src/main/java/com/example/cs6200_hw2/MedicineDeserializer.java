package com.example.cs6200_hw2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class MedicineDeserializer extends JsonDeserializer<Medicine> {
    @Override
    public Medicine deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode medicineNode = jsonParser.getCodec().readTree(jsonParser);

        Medicine medicine = new Medicine();
        if (medicineNode.has("docId")){
            medicine.setDocId(medicineNode.get("docId").textValue());
        }
        if (medicineNode.has("description")){
            medicine.setDescription(medicineNode.get("description").textValue());
        }
        return medicine;
    }
}
