package com.example.xbankbackend.services.transactionCategories;

import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.mappers.TransactionCategoryMapper;
import com.example.xbankbackend.models.TransactionCategory;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
import com.example.xbankbackend.repositories.TransactionKeywordsRepository;
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
@DisplayName("TransactionCategoriesService")
class TransactionCategoriesServiceTest {

    @Mock
    private TransactionKeywordsRepository keywordRepository;

    @Mock
    private TransactionCategoriesRepository categoriesRepository;

    @Mock
    private TransactionCategoryMapper categoryMapper;

    @Mock
    private TransactionCategoriesValidationService categoriesValidationService;

    @InjectMocks
    private TransactionCategoriesService service;

    @Nested
    @DisplayName("findCategory")
    class FindCategoryTests {

        @Test
        void shouldReturnOther_WhenDescriptionIsNull() {
            assertThat(service.findCategory(null)).isEqualTo("OTHER");
        }

        @Test
        void shouldReturnOther_WhenDescriptionIsEmpty() {
            assertThat(service.findCategory("")).isEqualTo("OTHER");
        }

        @Test
        void shouldReturnOther_WhenNoKeywordsMatch() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("такси", "TRANSPORT", OffsetDateTime.now())
            ));

            assertThat(service.findCategory("Покупка в аптеке")).isEqualTo("OTHER");
        }

        @Test
        void shouldReturnCategory_WhenKeywordMatches() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now())
            ));
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            assertThat(service.findCategory("Покупка в МАГНИТЕ")).isEqualTo("FOOD");
        }

        @Test
        void shouldReturnOther_WhenKeywordMatchesButCategoryIsDeleted() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now())
            ));
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(false);

            assertThat(service.findCategory("Покупка в МАГНИТЕ")).isEqualTo("OTHER");
        }
    }

    @Nested
    @DisplayName("getCategory")
    class GetCategoryTests {

        @Test
        void shouldReturnCategory_WhenExists() {
            TransactionCategory expected = new TransactionCategory();
            expected.setCode("FOOD");
            expected.setDisplayName("Еда");

            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");
            when(categoriesRepository.findByCode("FOOD")).thenReturn(expected);

            assertThat(service.getCategory("FOOD")).isEqualTo(expected);
        }

        @Test
        void shouldThrowCategoryNotFoundException_WhenNotExists() {
            doThrow(new CategoryNotFoundException("Not found"))
                    .when(categoriesValidationService).validateCategoryExists("UNKNOWN");

            assertThatThrownBy(() -> service.getCategory("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        void shouldReturnAllCategories() {
            List<TransactionCategory> expected = List.of(
                    new TransactionCategory("FOOD", "Еда", "#4CAF50", true),
                    new TransactionCategory("TRANSPORT", "Транспорт", "#2196F3", true)
            );

            when(categoriesRepository.findAllCategories()).thenReturn(expected);

            assertThat(service.getAllCategories()).hasSize(2).containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTests {

        @Test
        void shouldCreateCategory_WhenValid() {
            TransactionCategory input = new TransactionCategory();
            input.setCode("food");
            input.setDisplayName("Еда");
            input.setColorCode("#4CAF50");

            doNothing().when(categoriesValidationService).validateCategoryCode("FOOD");
            doNothing().when(categoriesValidationService).validateHexColor("#4CAF50");
            doNothing().when(categoriesValidationService).validateCategoryIsUnique("FOOD");

            service.createCategory(input);

            verify(categoriesRepository).create("FOOD", "Еда", "#4CAF50");
            assertThat(input.getIsActive()).isTrue();
        }

        @Test
        void shouldThrowException_WhenValidationFails() {
            TransactionCategory input = new TransactionCategory();
            input.setCode("FO OD");
            input.setColorCode("#FFF");

            doThrow(new IllegalArgumentException("Invalid code"))
                    .when(categoriesValidationService).validateCategoryCode("FO OD");

            assertThatThrownBy(() -> service.createCategory(input))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(categoriesRepository, never()).create(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategoryTests {

        @Test
        void shouldUpdateCategory_WhenValid() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setDisplayName("Новая Еда");
            request.setColorCode("#FF5722");

            TransactionCategory existing = new TransactionCategory("FOOD", "Еда", "#4CAF50", true);

            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");
            doNothing().when(categoriesValidationService).validateHexColor("#FF5722");
            when(categoriesRepository.findByCode("FOOD")).thenReturn(existing);

            service.updateCategory("FOOD", request);

            verify(categoryMapper).updateEntityFromRequest(request, existing);
            verify(categoriesRepository).update(existing);
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setColorCode("#FFF");

            doThrow(new CategoryNotFoundException("Not found"))
                    .when(categoriesValidationService).validateCategoryExists("UNKNOWN");

            assertThatThrownBy(() -> service.updateCategory("UNKNOWN", request))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategoryTests {

        @Test
        void shouldDeleteCategory_WhenExists() {
            doNothing().when(categoriesValidationService).validateCategoryExists("FOOD");

            service.deleteCategory("FOOD");

            verify(categoriesRepository).deleteByCode("FOOD");
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            doThrow(new CategoryNotFoundException("Not found"))
                    .when(categoriesValidationService).validateCategoryExists("UNKNOWN");

            assertThatThrownBy(() -> service.deleteCategory("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }
}
