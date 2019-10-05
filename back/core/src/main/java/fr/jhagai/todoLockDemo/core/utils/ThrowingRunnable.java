package fr.jhagai.todoLockDemo.core.utils;

public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;
}
