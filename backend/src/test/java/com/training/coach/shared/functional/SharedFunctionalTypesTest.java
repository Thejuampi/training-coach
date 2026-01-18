package com.training.coach.shared.functional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Shared Functional Types Tests")
class SharedFunctionalTypesTest {

    @Test
    @DisplayName("Result should create success result")
    void resultShouldCreateSuccessResult() {
        var result = com.training.coach.shared.functional.Result.success("test-value");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.value().isPresent()).isTrue();
        assertThat(result.value().get()).isEqualTo("test-value");
    }

    @Test
    @DisplayName("Result should create failure result")
    void resultShouldCreateFailureResult() {
        var error = new RuntimeException("Test error");
        var result = com.training.coach.shared.functional.Result.failure(error);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.error().isPresent()).isTrue();
        assertThat(result.error().get()).isEqualTo(error);
    }

    @Test
    @DisplayName("Either should create right value")
    void eitherShouldCreateRightValue() {
        var either = com.training.coach.shared.functional.Either.<String, Integer>right(42);

        assertThat(either.isRight()).isTrue();
        assertThat(either.isLeft()).isFalse();
        assertThat(either.right().isPresent()).isTrue();
        assertThat(either.right().get()).isEqualTo(42);
    }

    @Test
    @DisplayName("Either should create left value")
    void eitherShouldCreateLeftValue() {
        var either = com.training.coach.shared.functional.Either.<String, Integer>left("error-message");

        assertThat(either.isLeft()).isTrue();
        assertThat(either.isRight()).isFalse();
        assertThat(either.left().isPresent()).isTrue();
        assertThat(either.left().get()).isEqualTo("error-message");
    }
}
