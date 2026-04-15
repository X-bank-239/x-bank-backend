package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.exceptions.KeywordNotFoundException;
import com.example.xbankbackend.mappers.TransactionKeywordMapper;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TransactionKeywordService {

    private TransactionKeywordsRepository keywordRepository;
    private TransactionCategoriesRepository categoriesRepository;
    private TransactionKeywordMapper keywordMapper;

    public void createKeyword(TransactionKeyword keyword) {
        String word = keyword.getWord().toLowerCase().trim();
        String categoryCode = keyword.getCategoryCode();
        if (!categoriesRepository.existsByCode(categoryCode)) {
            throw new CategoryNotFoundException("Category with code " + categoryCode + " doesn't exist");
        }
        if (keywordRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new ConflictException("Category with code " + categoryCode + " and word " + word + " already exists");
        }
        keywordRepository.create(categoryCode, word);
    }

    public List<TransactionKeyword> getKeywordsByCategory(String categoryCode) {
        if (!categoriesRepository.existsByCode(categoryCode)) {
            throw new CategoryNotFoundException("Category with code " + categoryCode + " doesn't exist");
        }
        return keywordRepository.findByCode(categoryCode);
    }

    public List<TransactionKeyword> getAllKeywords() {
        return keywordRepository.findAllKeywords();
    }

    public TransactionKeyword updateKeyword(String categoryCode, String word, UpdateKeywordRequest request) {
        if (!keywordRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new KeywordNotFoundException("Category with code " + categoryCode + " and word " + word + " doesn't exist");
        }
        TransactionKeyword keyword = keywordRepository.findByCodeAndWord(categoryCode, word);
        keywordMapper.updateEntityFromRequest(request, keyword);
        keywordRepository.update(categoryCode, word, keyword);
        return keyword;
    }

    public void deleteKeyword(String categoryCode, String word) {
        if (!keywordRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new KeywordNotFoundException("Category with code " + categoryCode + " and word " + word + " doesn't exist");
        }
        keywordRepository.deleteByCodeAndWord(categoryCode, word);
    }
}
