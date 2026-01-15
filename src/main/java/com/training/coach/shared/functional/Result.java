package com.training.coach.shared.functional;

import java.util.Optional;

/**
 * Result type for handling success/failure scenarios with proper error messages.
 * This is an immutable value object used for error handling in functional core.
 *
 * @param <T> The type of the success value
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /**
     * Creates a success result containing the given value.
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failure result containing the given error.
     */
    static <T> Result<T> failure(Throwable error) {
        return new Failure<>(error);
    }

    /**
     * Returns true if this result is a success.
     */
    boolean isSuccess();

    /**
     * Returns true if this result is a failure.
     */
    boolean isFailure();

    /**
     * Returns the success value if present, otherwise empty.
     */
    Optional<T> value();

    /**
     * Returns the error if this is a failure, otherwise empty.
     */
    Optional<Throwable> error();

    /**
     * Returns the success value or throws the error if this is a failure.
     */
    T getOrThrow() throws Throwable;

    /**
     * Maps the success value using the given mapper.
     * If this is a failure, returns the failure unchanged.
     */
    <U> Result<U> map(java.util.function.Function<T, U> mapper);

    /**
     * Flat maps the success value using the given mapper.
     * If this is a failure, returns the failure unchanged.
     */
    <U> Result<U> flatMap(java.util.function.Function<T, Result<U>> mapper);

    /**
     * Success case containing a value.
     */
    record Success<T>(T _value) implements Result<T> {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public Optional<T> value() {
            return Optional.of(_value);
        }

        @Override
        public Optional<Throwable> error() {
            return Optional.empty();
        }

        @Override
        public T getOrThrow() {
            return _value;
        }

        @Override
        public <U> Result<U> map(java.util.function.Function<T, U> mapper) {
            try {
                return Result.success(mapper.apply(_value));
            } catch (Exception e) {
                return Result.failure(e);
            }
        }

        @Override
        public <U> Result<U> flatMap(java.util.function.Function<T, Result<U>> mapper) {
            try {
                return mapper.apply(_value);
            } catch (Exception e) {
                return Result.failure(e);
            }
        }
    }

    /**
     * Failure case containing an error.
     */
    record Failure<T>(Throwable _error) implements Result<T> {

        public Failure {
            if (_error == null) {
                _error = new RuntimeException("Unknown error");
            }
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public Optional<T> value() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> error() {
            return Optional.of(_error);
        }

        @Override
        public T getOrThrow() throws Throwable {
            throw _error;
        }

        @Override
        public <U> Result<U> map(java.util.function.Function<T, U> mapper) {
            return Result.failure(_error);
        }

        @Override
        public <U> Result<U> flatMap(java.util.function.Function<T, Result<U>> mapper) {
            return Result.failure(_error);
        }
    }
}
