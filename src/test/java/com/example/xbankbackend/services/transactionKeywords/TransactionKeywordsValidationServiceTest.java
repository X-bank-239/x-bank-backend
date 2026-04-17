package com.example.xbankbackend.services.transactionKeywords;

import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.exceptions.KeywordNotFoundException;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionKeywordsValidationService")
class TransactionKeywordsValidationServiceTest {

    @Mock
    private TransactionKeywordsRepository keywordsRepository;

    @InjectMocks
    private TransactionKeywordsValidationService service;

    @Nested
    @DisplayName("validateKeywordAndCodeExist")
    class ValidateKeywordAndCodeExistTests {

        @Test
        void shouldNotThrow_WhenKeywordExists() {
            when(keywordsRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateKeywordAndCodeExist("FOOD", "магнит"));
        }

        @Test
        void shouldThrowKeywordNotFoundException_WhenKeywordNotExists() {
            when(keywordsRepository.existsByCodeAndWord("FOOD", "несуществующий")).thenReturn(false);

            assertThatThrownBy(() -> service.validateKeywordAndCodeExist("FOOD", "несуществующий"))
                    .isInstanceOf(KeywordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateKeywordAndCodeAreUnique")
    class ValidateKeywordAndCodeAreUniqueTests {

        @Test
        void shouldNotThrow_WhenKeywordIsUnique() {
            when(keywordsRepository.existsByCodeAndWord("FOOD", "новый")).thenReturn(false);

            assertThatNoException().isThrownBy(() -> service.validateKeywordAndCodeAreUnique("FOOD", "новый"));
        }

        @Test
        void shouldThrowConflictException_WhenKeywordAlreadyExists() {
            when(keywordsRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(true);

            assertThatThrownBy(() -> service.validateKeywordAndCodeAreUnique("FOOD", "магнит"))
                    .isInstanceOf(ConflictException.class);
        }
    }
}
