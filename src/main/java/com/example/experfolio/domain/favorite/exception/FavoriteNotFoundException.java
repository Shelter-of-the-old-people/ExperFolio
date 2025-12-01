package com.example.experfolio.domain.favorite.exception;

/**
 * 즐겨찾기를 찾을 수 없을 때 발생하는 예외
 * 존재하지 않는 즐겨찾기를 제거하려고 할 때 발생
 */
public class FavoriteNotFoundException extends RuntimeException {

    public FavoriteNotFoundException(String message) {
        super(message);
    }

    public FavoriteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
