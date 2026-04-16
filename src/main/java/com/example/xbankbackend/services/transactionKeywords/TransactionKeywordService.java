package com.example.xbankbackend.services.transactionKeywords;

import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.mappers.TransactionKeywordMapper;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TransactionKeywordService {

    private TransactionKeywordsRepository keywordRepository;
    private TransactionCategoriesRepository categoriesRepository;
    private TransactionKeywordMapper keywordMapper;

    private final TransactionKeywordsValidationService keywordsValidationService;
    private final TransactionCategoriesValidationService categoriesValidationService;

    public void createKeyword(TransactionKeyword keyword) {
        String word = keyword.getWord().toLowerCase().trim();
        String categoryCode = keyword.getCategoryCode();

        categoriesValidationService.validateCategoryExists(categoryCode);
        keywordsValidationService.validateKeywordAndCodeAreUnique(categoryCode, word);

        keywordRepository.create(categoryCode, word);
    }

    public List<TransactionKeyword> getKeywordsByCategory(String categoryCode) {
        categoriesValidationService.validateCategoryExists(categoryCode);

        return keywordRepository.findByCode(categoryCode);
    }

    public List<TransactionKeyword> getAllKeywords() {
        return keywordRepository.findAllKeywords();
    }

    public TransactionKeyword updateKeyword(String categoryCode, String word, UpdateKeywordRequest request) {
        keywordsValidationService.validateKeywordAndCodeExist(categoryCode, word);

        TransactionKeyword keyword = keywordRepository.findByCodeAndWord(categoryCode, word);
        keywordMapper.updateEntityFromRequest(request, keyword);
        keywordRepository.update(categoryCode, word, keyword);

        return keyword;
    }

    public void deleteKeyword(String categoryCode, String word) {
        keywordsValidationService.validateKeywordAndCodeExist(categoryCode, word);

        keywordRepository.deleteByCodeAndWord(categoryCode, word);
    }
}
