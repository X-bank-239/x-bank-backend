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
            throw new CategoryNotFoundException("Category with code " + categoryCode + " doesn't exist");
        }
    }

    public void validateCategoryIsUnique(String categoryCode) {
        if (categoriesRepository.existsByCode(categoryCode)) {
            throw new ConflictException("Category with code " + categoryCode + " already exists");
        }
    }

    public void validateCategoryCode(String categoryCode) {
        if (categoryCode.contains(" ")) {
            throw new IllegalArgumentException("Code cannot contain spaces");
        }
    }

    public void validateHexColor(String colorCode) {
        if (!HEX_PATTERN.matcher(colorCode).matches()) {
            throw new IllegalArgumentException("Color code " + colorCode + " is not valid HEX");
        }
    }
}
