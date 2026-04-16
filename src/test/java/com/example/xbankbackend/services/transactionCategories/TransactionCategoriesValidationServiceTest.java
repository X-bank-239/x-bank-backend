package com.example.xbankbackend.services.transactionCategories;

import com.example.xbankbackend.exceptions.CategoryNotFoundException;
import com.example.xbankbackend.exceptions.ConflictException;
import com.example.xbankbackend.repositories.TransactionCategoriesRepository;
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
@DisplayName("TransactionCategoriesValidationService")
class TransactionCategoriesValidationServiceTest {

    @Mock
    private TransactionCategoriesRepository categoriesRepository;

    @InjectMocks
    private TransactionCategoriesValidationService service;

    @Nested
    @DisplayName("validateCategoryExists")
    class ValidateCategoryExistsTests {

        @Test
        void shouldNotThrow_WhenCategoryExists() {
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateCategoryExists("FOOD"));
        }

        @Test
        void shouldThrowCategoryNotFoundException_WhenCategoryNotExists() {
            when(categoriesRepository.existsByCode("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> service.validateCategoryExists("UNKNOWN"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateCategoryIsUnique")
    class ValidateCategoryIsUniqueTests {

        @Test
        void shouldNotThrow_WhenCategoryIsUnique() {
            when(categoriesRepository.existsByCode("NEW")).thenReturn(false);

            assertThatNoException().isThrownBy(() -> service.validateCategoryIsUnique("NEW"));
        }

        @Test
        void shouldThrowConflictException_WhenCategoryAlreadyExists() {
            when(categoriesRepository.existsByCode("FOOD")).thenReturn(true);

            assertThatThrownBy(() -> service.validateCategoryIsUnique("FOOD"))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("validateCategoryCode")
    class ValidateCategoryCodeTests {

        @Test
        void shouldNotThrow_WhenCodeHasNoSpaces() {
            assertThatNoException().isThrownBy(() -> service.validateCategoryCode("FOOD"));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenCodeHasSpaces() {
            assertThatThrownBy(() -> service.validateCategoryCode("FO OD"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validateHexColor")
    class ValidateHexColorTests {

        @Test
        void shouldNotThrow_WhenColorIsValidShortHex() {
            assertThatNoException().isThrownBy(() -> service.validateHexColor("#FFF"));
        }

        @Test
        void shouldNotThrow_WhenColorIsValidLongHex() {
            assertThatNoException().isThrownBy(() -> service.validateHexColor("#123ABC"));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenColorIsInvalid() {
            assertThatThrownBy(() -> service.validateHexColor("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenColorMissingHash() {
            assertThatThrownBy(() -> service.validateHexColor("123ABC"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
