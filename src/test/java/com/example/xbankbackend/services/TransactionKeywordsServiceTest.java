package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.exceptions.KeywordNotFoundException;
import com.example.xbankbackend.mappers.TransactionKeywordMapper;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionKeywordsServiceTest {

    @Mock private TransactionKeywordsRepository keywordRepository;
    @Mock private TransactionCategoriesRepository categoriesRepository;
    @Mock private TransactionKeywordMapper keywordMapper;

    private TransactionKeywordService service;

    @BeforeEach
    void setUp() {
        service = new TransactionKeywordService(keywordRepository, categoriesRepository, keywordMapper);
    }

    @Nested
    @DisplayName("createKeyword")
    class CreateKeywordTests {

        @Test
        void shouldCreateKeyword_WhenValid() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("  Магнит  ");
            input.setCategoryCode("FOOD");

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);
            when(keywordRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(false);

            service.createKeyword(input);

            verify(keywordRepository).create("FOOD", "магнит");
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("магнит");
            input.setCategoryCode("UNKNOWN");

            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.createKeyword(input))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessage("Category with code UNKNOWN doesn't exist");
        }

        @Test
        void shouldThrowException_WhenKeywordAlreadyExists() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("магнит");
            input.setCategoryCode("FOOD");

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);
            when(keywordRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(true);

            assertThatThrownBy(() -> service.createKeyword(input))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Category with code FOOD and word магнит already exists");
        }
    }

    @Nested
    @DisplayName("getKeywordsByCategory")
    class GetKeywordsByCategoryTests {

        @Test
        void shouldReturnKeywords_WhenCategoryExists() {
            List<TransactionKeyword> expected = List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("пятерочка", "FOOD", OffsetDateTime.now())
            );

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);
            when(keywordRepository.findByCode("FOOD")).thenReturn(expected);

            List<TransactionKeyword> result = service.getKeywordsByCategory("FOOD");

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.getKeywordsByCategory("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllKeywords")
    class GetAllKeywordsTests {

        @Test
        void shouldReturnAllKeywords() {
            List<TransactionKeyword> expected = List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("такси", "TRANSPORT", OffsetDateTime.now())
            );
            when(keywordRepository.findAllKeywords()).thenReturn(expected);

            List<TransactionKeyword> result = service.getAllKeywords();

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("updateKeyword")
    class UpdateKeywordTests {

        @Test
        void shouldUpdateKeyword_WhenExists() {
            UpdateKeywordRequest request = new UpdateKeywordRequest();
            request.setWord("новый-магнит");

            TransactionKeyword existing = new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now());

            when(keywordRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(true);
            when(keywordRepository.findByCodeAndWord("FOOD", "магнит")).thenReturn(existing);

            service.updateKeyword("FOOD", "магнит", request);

            verify(keywordMapper).updateEntityFromRequest(request, existing);
            verify(keywordRepository).update("FOOD", "магнит", existing);
        }

        @Test
        void shouldThrowException_WhenKeywordNotFound() {
            UpdateKeywordRequest request = new UpdateKeywordRequest();
            request.setWord("новый");

            when(keywordRepository.existsByCodeAndWord("FOOD", "старый")).thenReturn(false);

            assertThatThrownBy(() -> service.updateKeyword("FOOD", "старый", request))
                    .isInstanceOf(KeywordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteKeyword")
    class DeleteKeywordTests {

        @Test
        void shouldDeleteKeyword_WhenExists() {
            when(keywordRepository.existsByCodeAndWord("FOOD", "магнит")).thenReturn(true);

            service.deleteKeyword("FOOD", "магнит");

            verify(keywordRepository).deleteByCodeAndWord("FOOD", "магнит");
        }

        @Test
        void shouldThrowException_WhenKeywordNotFound() {
            when(keywordRepository.existsByCodeAndWord("FOOD", "несуществующий")).thenReturn(false);

            assertThatThrownBy(() -> service.deleteKeyword("FOOD", "несуществующий"))
                    .isInstanceOf(KeywordNotFoundException.class);
        }
    }
}