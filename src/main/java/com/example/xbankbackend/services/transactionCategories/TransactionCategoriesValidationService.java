package com.example.xbankbackend.services.transactionCategories;

import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class TransactionCategoriesValidationService {

    private static final Pattern HEX_PATTERN = Pattern.compile(
            "^#([0-9A-F]{3}|[0-9A-F]{6})$"
    );

    private TransactionCategoriesRepository categoriesRepository;

    public void validateCategoryExists(String categoryCode) {
        if (!categoriesRepository.existsByCode(categoryCode)) {
            throw new CategoryNotFoundException("Категория с кодом " + categoryCode + " не существует");
        }
    }

    public void validateCategoryIsUnique(String categoryCode) {
        if (categoriesRepository.existsByCode(categoryCode)) {
            throw new ConflictException("Категория с кодом " + categoryCode + " уже существует");
        }
    }

    public void validateCategoryCode(String categoryCode) {
        if (categoryCode.contains(" ")) {
            throw new IllegalArgumentException("Код не может содержать пробелы");
        }
    }

    public void validateHexColor(String colorCode) {
        if (!HEX_PATTERN.matcher(colorCode).matches()) {
            throw new IllegalArgumentException("Цвет " + colorCode + " - недопустимый HEX");
        }
    }
}
