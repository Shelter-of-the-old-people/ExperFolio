package com.example.experfolio.domain.favorite.exception;

/**
 * 중복 즐겨찾기 예외
 * 이미 즐겨찾기한 구직자를 다시 즐겨찾기하려고 할 때 발생
 */
public class DuplicateFavoriteException extends RuntimeException {

    public DuplicateFavoriteException(String message) {
        super(message);
    }

    public DuplicateFavoriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
