package com.example.xbankbackend.services.transactionKeywords;

import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.exceptions.KeywordNotFoundException;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TransactionKeywordsValidationService {

    private TransactionKeywordsRepository keywordsRepository;

    public void validateKeywordAndCodeExist(String categoryCode, String word) {
        if (!keywordsRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new KeywordNotFoundException("Category with code " + categoryCode + " and word " + word + " doesn't exist");
        }
    }

    public void validateKeywordAndCodeAreUnique(String categoryCode, String word) {
        if (keywordsRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new ConflictException("Category with code " + categoryCode + " and word " + word + " already exists");
        }
    }
}
