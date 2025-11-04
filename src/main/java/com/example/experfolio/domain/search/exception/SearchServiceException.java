package com.example.experfolio.domain.search.exception;

/**
 * 검색 서비스 예외
 */
public class SearchServiceException extends RuntimeException {

    public SearchServiceException(String message) {
        super(message);
    }

    public SearchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
