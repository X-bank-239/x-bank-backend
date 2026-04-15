package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.mappers.TransactionCategoryMapper;
import com.example.xbankbackend.models.TransactionCategory;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCategoriesServiceTest {

    @Mock
    private TransactionKeywordsRepository keywordRepository;

    @Mock
    private TransactionCategoriesRepository categoriesRepository;

    @Mock
    private TransactionCategoryMapper categoryMapper;

    private TransactionCategoriesService service;

    @BeforeEach
    void setUp() {
        service = new TransactionCategoriesService(keywordRepository, categoriesRepository, categoryMapper);
    }

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
            String result = service.findCategory("Покупка в аптеке");

            assertThat(result).isEqualTo("OTHER");
        }

        @Test
        void shouldReturnCategory_WhenKeywordMatches() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("такси", "TRANSPORT", OffsetDateTime.now())
            ));
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            String result = service.findCategory("Покупка в МАГНИТЕ");

            assertThat(result).isEqualTo("FOOD");
        }

        @Test
        void shouldReturnOther_WhenKeywordMatchesButCategoryIsDeleted() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now())
            ));
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(false);

            String result = service.findCategory("Покупка в МАГНИТЕ");

            assertThat(result).isEqualTo("OTHER");
        }

        @Test
        void shouldReturnFirstMatchingCategory_WhenMultipleKeywordsMatch() {
            when(keywordRepository.findAllKeywords()).thenReturn(List.of(
                    new TransactionKeyword("еда", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("магнит", "FOOD", OffsetDateTime.now()),
                    new TransactionKeyword("продукты", "FOOD", OffsetDateTime.now())
            ));
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            String result = service.findCategory("Покупка продуктов в магните");

            assertThat(result).isEqualTo("FOOD");
            verify(categoriesRepository, times(1)).existsByCode("FOOD");
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

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);
            when(categoriesRepository.findByCode("FOOD")).thenReturn(expected);

            TransactionCategory result = service.getCategory("FOOD");

            assertThat(result).isEqualTo(expected);
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.getCategory("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        void shouldReturnAllActiveCategories() {
            List<TransactionCategory> expected = List.of(
                    new TransactionCategory("FOOD", "Еда", "#4CAF50", true),
                    new TransactionCategory("TRANSPORT", "Транспорт", "#2196F3", true)
            );
            when(categoriesRepository.findAllCategories()).thenReturn(expected);

            List<TransactionCategory> result = service.getAllCategories();

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
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

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(false);

            service.createCategory(input);

            verify(categoriesRepository).create("FOOD", "Еда", "#4CAF50");
            verify(categoriesRepository).create(anyString(), anyString(), anyString());
        }

        @Test
        void shouldThrowException_WhenCodeContainsSpaces() {
            TransactionCategory input = new TransactionCategory();
            input.setCode("FO OD");
            input.setDisplayName("Еда");
            input.setColorCode("#4CAF50");

            assertThatThrownBy(() -> service.createCategory(input))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenColorCodeIsInvalid() {
            TransactionCategory input = new TransactionCategory();
            input.setCode("FOOD");
            input.setDisplayName("Еда");
            input.setColorCode("invalid");

            assertThatThrownBy(() -> service.createCategory(input))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenCategoryAlreadyExists() {
            TransactionCategory input = new TransactionCategory();
            input.setCode("FOOD");
            input.setDisplayName("Еда");
            input.setColorCode("#4CAF50");

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            assertThatThrownBy(() -> service.createCategory(input))
                    .isInstanceOf(ConflictException.class);
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

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);
            when(categoriesRepository.findByCode("FOOD")).thenReturn(existing);

            service.updateCategory("FOOD", request);

            verify(categoryMapper).updateEntityFromRequest(request, existing);
            verify(categoriesRepository).update(existing);
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setDisplayName("Новая Еда");
            request.setColorCode("#FF5722");

            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.updateCategory("UNKNOWN", request))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        void shouldThrowException_WhenInvalidColorCode() {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setDisplayName("Новая Еда");
            request.setColorCode("not-a-hex");

            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            assertThatThrownBy(() -> service.updateCategory("FOOD", request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategoryTests {

        @Test
        void shouldDeleteCategory_WhenExists() {
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            service.deleteCategory("FOOD");

            verify(categoriesRepository).deleteByCode("FOOD");
        }

        @Test
        void shouldThrowException_WhenCategoryNotFound() {
            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.deleteCategory("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }
}
