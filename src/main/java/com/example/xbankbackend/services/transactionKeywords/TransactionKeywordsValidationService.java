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
            throw new KeywordNotFoundException("Категория с кодом " + categoryCode + " и словом " + word + " не существует");
        }
    }

    public void validateKeywordAndCodeAreUnique(String categoryCode, String word) {
        if (keywordsRepository.existsByCodeAndWord(categoryCode, word)) {
            throw new ConflictException("Категория с кодом " + categoryCode + " и словом " + word + " уже существует");
        }
    }
}
