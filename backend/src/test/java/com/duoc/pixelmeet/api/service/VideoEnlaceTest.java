package com.duoc.pixelmeet.api.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VideoEnlaceTest {

    @ParameterizedTest
    @CsvSource({
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ, YOUTUBE, dQw4w9WgXcQ",
            "https://youtube.com/watch?feature=share&v=dQw4w9WgXcQ, YOUTUBE, dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ#t=30, YOUTUBE, dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abc, YOUTUBE, dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ, YOUTUBE, dQw4w9WgXcQ",
            "https://vimeo.com/76979871, VIMEO, 76979871"
    })
    void reconoceEnlacesValidos(String url, String plataforma, String id) {
        assertThat(VideoEnlace.reconocer(url)).hasValueSatisfying(v -> {
            assertThat(v.plataforma()).isEqualTo(plataforma);
            assertThat(v.id()).isEqualTo(id);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com.evil.com/watch?v=dQw4w9WgXcQ",
            "https://evil.com/?u=https://youtu.be/dQw4w9WgXcQ",
            "https://youtu.be/corto",
            "https://vimeo.com/abc",
            "https://youtu.be/dQw4w9WgXcQ extra",
            ""
    })
    void rechazaTodoLoDemas(String url) {
        assertThat(VideoEnlace.reconocer(url)).isEmpty();
    }
}
