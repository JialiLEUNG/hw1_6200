package com.example.cs6200_hw2;

import java.io.IOException;

public interface MedicineService {

    Medicine findById(String id) throws IOException;
    Page<Medicine> search(String query) throws IOException;
    Page<Medicine> next(Page page) throws IOException;
    void save(Medicine medicine) throws IOException;

}
