package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateCategoryRequest;
import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.mappers.TransactionCategoryMapper;
import com.example.xbankbackend.models.TransactionCategory;
import com.example.xbankbackend.services.TransactionCategoriesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/transactions/categories")
public class CategoriesController {

    private TransactionCategoriesService categoriesService;
    private TransactionCategoryMapper transactionCategoryMapper;

    @GetMapping("/{code}")
    public ResponseEntity<TransactionCategory> getCategories(@PathVariable String code) {
        log.info("Getting category with code {}", code);

        TransactionCategory category = categoriesService.getCategory(code);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransactionCategory>> getCategories() {
        log.info("Getting all categories");

        List<TransactionCategory> categories = categoriesService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @PostMapping("")
    public ResponseEntity<TransactionCategory> createCategory(@Valid @RequestBody CreateCategoryRequest categoryRequest) {
        log.info("Creating new category {}", categoryRequest.getCode());

        TransactionCategory category = transactionCategoryMapper.requestToCategory(categoryRequest);
        categoriesService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PatchMapping("/{code}")
    public ResponseEntity<TransactionCategory> updateCategory(@PathVariable String code, @RequestBody UpdateCategoryRequest request) {
        log.info("Updating category with code {}, request: {}", code, request.toString());

        TransactionCategory category = categoriesService.updateCategory(code, request);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String code) {
        log.info("Deleting category with code {}", code);

        categoriesService.deleteCategory(code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
