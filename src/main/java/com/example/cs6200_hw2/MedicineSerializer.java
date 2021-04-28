package com.example.cs6200_hw2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class MedicineSerializer extends JsonSerializer<Medicine> {
    @Override
    public void serialize(Medicine medicine, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("docId", medicine.getDocId());
        jsonGenerator.writeStringField("description", medicine.getDescription());
        jsonGenerator.writeEndObject();
    }
}
