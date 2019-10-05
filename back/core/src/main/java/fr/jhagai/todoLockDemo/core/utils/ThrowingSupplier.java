package fr.jhagai.todoLockDemo.core.utils;

public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}
