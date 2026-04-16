package com.example.xbankbackend.services.transactionCategories;

import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.mappers.TransactionCategoryMapper;
import com.example.xbankbackend.models.TransactionCategory;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TransactionCategoriesService {

    private TransactionKeywordsRepository keywordRepository;
    private TransactionCategoriesRepository categoriesRepository;
    private TransactionCategoryMapper categoryMapper;

    private final TransactionCategoriesValidationService categoriesValidationService;

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
        categoriesValidationService.validateCategoryExists(code);

        return categoriesRepository.findByCode(code);
    }

    public List<TransactionCategory> getAllCategories() {
        return categoriesRepository.findAllCategories();
    }

    public void createCategory(TransactionCategory category) {
        String code = category.getCode().toUpperCase();

        categoriesValidationService.validateCategoryCode(code);
        categoriesValidationService.validateHexColor(category.getColorCode());
        categoriesValidationService.validateCategoryIsUnique(code);

        category.setCode(code);
        category.setIsActive(true);

        categoriesRepository.create(category.getCode(), category.getDisplayName(), category.getColorCode());
    }

    public TransactionCategory updateCategory(String code, UpdateCategoryRequest request) {
        categoriesValidationService.validateCategoryExists(code);
        categoriesValidationService.validateHexColor(request.getColorCode());

        TransactionCategory category = categoriesRepository.findByCode(code);

        categoryMapper.updateEntityFromRequest(request, category);
        categoriesRepository.update(category);

        return category;
    }

    public void deleteCategory(String code) {
        categoriesValidationService.validateCategoryExists(code);

        categoriesRepository.deleteByCode(code);
    }
}
