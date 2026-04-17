package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateKeywordRequest;
import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.mappers.TransactionKeywordMapper;
import com.example.xbankbackend.models.TransactionKeyword;
import com.example.xbankbackend.services.transactionKeywords.TransactionKeywordService;
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
@RequestMapping("/transactions/keywords")
public class KeywordsController {

    private TransactionKeywordService keywordService;
    private TransactionKeywordMapper keywordMapper;

    @GetMapping("/{categoryCode}")
    public ResponseEntity<List<TransactionKeyword>> getKeywordsByCategory(@PathVariable String categoryCode) {
        log.info("Getting keywords for category {}", categoryCode);

        List<TransactionKeyword> keywords = keywordService.getKeywordsByCategory(categoryCode);
        return ResponseEntity.status(HttpStatus.OK).body(keywords);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransactionKeyword>> getAllKeywords() {
        log.info("Getting all keywords");

        List<TransactionKeyword> keywords = keywordService.getAllKeywords();
        return ResponseEntity.status(HttpStatus.OK).body(keywords);
    }

    @PostMapping("")
    public ResponseEntity<TransactionKeyword> createKeyword(@Valid @RequestBody CreateKeywordRequest request) {
        log.info("Creating keyword {}", request.toString());

        TransactionKeyword keyword = keywordMapper.requestToKeyword(request);
        keywordService.createKeyword(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(keyword);
    }

    @PatchMapping("/{categoryCode}")
    public ResponseEntity<TransactionKeyword> updateKeyword(@PathVariable String categoryCode,
                                                            @RequestParam String word,
                                                            @Valid @RequestBody UpdateKeywordRequest request) {
        log.info("Updating keyword, category {}, word: {}, request: {}", categoryCode, word, request.toString());

        TransactionKeyword keyword = keywordService.updateKeyword(categoryCode, word, request);
        return ResponseEntity.status(HttpStatus.OK).body(keyword);
    }

    @DeleteMapping("/{categoryCode}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable String categoryCode,
                                              @RequestParam String word) {
        log.info("Deleting keyword, category: {}, word: {}", categoryCode, word);

        keywordService.deleteKeyword(categoryCode, word);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
