package com.training.coach.shared.functional;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Either type for handling values that can be of one of two types.
 * Typically used for error handling where Left represents failure and Right represents success.
 *
 * @param <L> The type of the left value (typically error/failure)
 * @param <R> The type of the right value (typically success)
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    /**
     * Creates a Left value (typically representing failure/error).
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Creates a Right value (typically representing success).
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Returns true if this is a Left value.
     */
    boolean isLeft();

    /**
     * Returns true if this is a Right value.
     */
    boolean isRight();

    /**
     * Returns Left value if present, otherwise empty.
     */
    Optional<L> left();

    /**
     * Returns Right value if present, otherwise empty.
     */
    Optional<R> right();

    /**
     * Returns the Right value or the given default if this is Left.
     */
    R getOrElse(R defaultValue);

    /**
     * Returns the Right value or throws if this is Left.
     */
    R get() throws NoSuchElementException;

    /**
     * Maps the Right value using the given mapper.
     * If this is Left, returns Left unchanged.
     */
    <U> Either<L, U> map(java.util.function.Function<R, U> mapper);

    /**
     * Flat maps the Right value using the given mapper.
     * If this is Left, returns Left unchanged.
     */
    <U> Either<L, U> flatMap(java.util.function.Function<R, Either<L, U>> mapper);

    /**
     * Maps the Left value using the given mapper.
     * If this is Right, returns Right unchanged.
     */
    <L2> Either<L2, R> mapLeft(java.util.function.Function<L, L2> mapper);

    /**
     * Swaps Left and Right sides.
     */
    Either<R, L> swap();

    /**
     * Applies one of two functions depending on whether this is Left or Right.
     */
    <T> T fold(java.util.function.Function<L, T> onLeft, java.util.function.Function<R, T> onRight);

    /**
     * Left case containing a value (typically error/failure).
     */
    record Left<L, R>(L value) implements Either<L, R> {

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public Optional<L> left() {
            return Optional.of(value);
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }

        @Override
        public R getOrElse(R defaultValue) {
            return defaultValue;
        }

        @Override
        public R get() throws NoSuchElementException {
            throw new NoSuchElementException("Cannot get value from Left: " + value);
        }

        @Override
        public <U> Either<L, U> map(java.util.function.Function<R, U> mapper) {
            return new Left<>(value);
        }

        @Override
        public <U> Either<L, U> flatMap(java.util.function.Function<R, Either<L, U>> mapper) {
            return new Left<>(value);
        }

        @Override
        public <L2> Either<L2, R> mapLeft(java.util.function.Function<L, L2> mapper) {
            return new Left<>(mapper.apply(value));
        }

        @Override
        public Either<R, L> swap() {
            return new Right<>(value);
        }

        @Override
        public <T> T fold(java.util.function.Function<L, T> onLeft, java.util.function.Function<R, T> onRight) {
            return onLeft.apply(value);
        }
    }

    /**
     * Right case containing a value (typically success).
     */
    record Right<L, R>(R value) implements Either<L, R> {

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }

        @Override
        public Optional<R> right() {
            return Optional.of(value);
        }

        @Override
        public R getOrElse(R defaultValue) {
            return value;
        }

        @Override
        public R get() {
            return value;
        }

        @Override
        public <U> Either<L, U> map(java.util.function.Function<R, U> mapper) {
            return new Right<>(mapper.apply(value));
        }

        @Override
        public <U> Either<L, U> flatMap(java.util.function.Function<R, Either<L, U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <L2> Either<L2, R> mapLeft(java.util.function.Function<L, L2> mapper) {
            return new Right<>(value);
        }

        @Override
        public Either<R, L> swap() {
            return new Left<>(value);
        }

        @Override
        public <T> T fold(java.util.function.Function<L, T> onLeft, java.util.function.Function<R, T> onRight) {
            return onRight.apply(value);
        }
    }
}
