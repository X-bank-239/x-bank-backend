package com.example.xbankbackend.services.transactionKeywords;

import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.exceptions.KeywordNotFoundException;
import com.example.xbankbackend.mappers.TransactionKeywordMapper;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
import com.example.xbankbackend.services.transactionCategories.TransactionCategoriesValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionKeywordService")
class TransactionKeywordsServiceTest {

    @Mock
    private TransactionKeywordsRepository keywordRepository;

    @Mock
    private TransactionKeywordMapper keywordMapper;

    @Mock
    private TransactionKeywordsValidationService keywordsValidationService;

    @Mock
    private TransactionCategoriesValidationService categoriesValidationService;

    @InjectMocks
    private TransactionKeywordService service;

    @Nested
    @DisplayName("createKeyword")
    class CreateKeywordTests {

        @Test
        void shouldCreateKeyword_WhenValid() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("  Магнит  ");
            input.setCategoryCode("FOOD");

            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");
            doNothing().when(keywordsValidationService).validateKeywordAndCodeAreUnique("FOOD", "магнит");

            service.createKeyword(input);

            verify(keywordRepository).create("FOOD", "магнит");
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("магнит");
            input.setCategoryCode("UNKNOWN");

            doThrow(new CategoryNotFoundException("Not found"))
                    .when(categoriesValidationService).validateCategoryExists("UNKNOWN");

            assertThatThrownBy(() -> service.createKeyword(input))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(keywordRepository, never()).create(any(), any());
        }

        @Test
        void shouldThrowException_WhenKeywordAlreadyExists() {
            TransactionKeyword input = new TransactionKeyword();
            input.setWord("магнит");
            input.setCategoryCode("FOOD");

            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");
            doThrow(new ConflictException("Exists"))
                    .when(keywordsValidationService).validateKeywordAndCodeAreUnique("FOOD", "магнит");

            assertThatThrownBy(() -> service.createKeyword(input))
                    .isInstanceOf(ConflictException.class);
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

            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");
            when(keywordRepository.findByCode("FOOD")).thenReturn(expected);

            assertThat(service.getKeywordsByCategory("FOOD")).hasSize(2).containsExactlyElementsOf(expected);
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            doThrow(new CategoryNotFoundException("Not found"))
                    .when(categoriesValidationService).validateCategoryExists("UNKNOWN");

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

            assertThat(service.getAllKeywords()).hasSize(2).containsExactlyElementsOf(expected);
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

            doNothing().when(keywordsValidationService).validateKeywordAndCodeExist("FOOD", "магнит");
            when(keywordRepository.findByCodeAndWord("FOOD", "магнит")).thenReturn(existing);

            service.updateKeyword("FOOD", "магнит", request);

            verify(keywordMapper).updateEntityFromRequest(request, existing);
            verify(keywordRepository).update("FOOD", "магнит", existing);
        }

        @Test
        void shouldThrowException_WhenKeywordNotFound() {
            UpdateKeywordRequest request = new UpdateKeywordRequest();

            doThrow(new KeywordNotFoundException("Not found"))
                    .when(keywordsValidationService).validateKeywordAndCodeExist("FOOD", "старый");

            assertThatThrownBy(() -> service.updateKeyword("FOOD", "старый", request))
                    .isInstanceOf(KeywordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteKeyword")
    class DeleteKeywordTests {

        @Test
        void shouldDeleteKeyword_WhenExists() {
            doNothing().when(keywordsValidationService).validateKeywordAndCodeExist("FOOD", "магнит");

            service.deleteKeyword("FOOD", "магнит");

            verify(keywordRepository).deleteByCodeAndWord("FOOD", "магнит");
        }

        @Test
        void shouldThrowException_WhenKeywordNotFound() {
            doThrow(new KeywordNotFoundException("Not found"))
                    .when(keywordsValidationService).validateKeywordAndCodeExist("FOOD", "несуществующий");

            assertThatThrownBy(() -> service.deleteKeyword("FOOD", "несуществующий"))
                    .isInstanceOf(KeywordNotFoundException.class);
        }
    }
}
