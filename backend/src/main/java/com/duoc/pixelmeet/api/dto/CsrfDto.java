package com.duoc.pixelmeet.api.dto;

/** Token CSRF y nombre del header en que el cliente debe reenviarlo. */
public record CsrfDto(String header, String token) {
}
