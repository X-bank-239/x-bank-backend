package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.mappers.TransactionCategoryMapper;
import com.example.xbankbackend.models.TransactionCategory;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class TransactionCategoriesService {

    private TransactionKeywordsRepository keywordRepository;
    private TransactionCategoriesRepository categoriesRepository;
    private TransactionCategoryMapper categoryMapper;

    public String findCategory(String description) {
        if (description == null || description.isEmpty()) {
            return "OTHER";
        }

        String text = description.toLowerCase();

        // TODO: закэшировать
        List<TransactionKeyword> keywords = keywordRepository.findAllKeywords();

        // у кейвордов - настоящее удаление, у категорий - soft-delete
        for (TransactionKeyword keyword : keywords) {
            if (text.contains(keyword.getWord())) {
                String code = keyword.getCategoryCode();
                if (categoriesRepository.existsByCode(code)) {
                    return code;
                }
            }
        }

        return "OTHER";
    }

    public TransactionCategory getCategory(String code) {
        if (!categoriesRepository.existsByCode(code)) {
            throw new CategoryNotFoundException("Category with code " + code + " doesn't exist");
        }
        return categoriesRepository.findByCode(code);
    }

    public List<TransactionCategory> getAllCategories() {
        return categoriesRepository.findAllCategories();
    }

    public void createCategory(TransactionCategory category) {
        String code = category.getCode().toUpperCase();
        if (code.contains(" ")) {
            throw new IllegalArgumentException("Code cannot contain spaces");
        }
        if (!isValidHex(category.getColorCode())) {
            throw new IllegalArgumentException("Color code is not valid HEX");
        }
        if (categoriesRepository.existsByCode(code)) {
            throw new ConflictException("Category with code " + category.getCode() + " already exists");
        }
        category.setCode(code);
        category.setIsActive(true);
        categoriesRepository.create(category.getCode(), category.getDisplayName(), category.getColorCode());
    }

    public TransactionCategory updateCategory(String code, UpdateCategoryRequest request) {
        if (!categoriesRepository.existsByCode(code)) {
            throw new CategoryNotFoundException("Category with code " + code + " doesn't exist");
        }
        if (!isValidHex(request.getColorCode())) {
            throw new IllegalArgumentException("Color code is not valid HEX");
        }
        TransactionCategory category = categoriesRepository.findByCode(code);
        categoryMapper.updateEntityFromRequest(request, category);
        categoriesRepository.update(category);
        return category;
    }

    public void deleteCategory(String code) {
        if (!categoriesRepository.existsByCode(code)) {
            throw new CategoryNotFoundException("Category with code " + code + " doesn't exist");
        }
        categoriesRepository.deleteByCode(code);
    }

    private boolean isValidHex(String colorCode) {
        Pattern hexPattern = Pattern.compile("^#([0-9A-F]{3}|[0-9A-F]{6})$");
        Matcher matcher = hexPattern.matcher(colorCode);
        return matcher.matches();
    }
}
