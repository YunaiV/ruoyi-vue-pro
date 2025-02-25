package cn.iocoder.yudao.framework.common.util.collection;

/*
 * @Description: $
 * @Author: c-tao
 * @Date: $
 */
import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.util.Spliterator;
import java.util.function.Consumer;

public class PageUtils {

    /**
     * Fetches all pages of results starting from the first page as a Stream.
     *
     * @param <T>          The type representing a page of results.
     * @param firstPage    The first page of results.
     * @param haveNextPage   A method to decide if the current page is the last page.
     * @param getNextPage  A method to produce the next page given the current page.
     * @return A Stream of all pages of results.
     */
    public static <T> Stream<T> getAllPages(
        T firstPage,
        Predicate<T> haveNextPage,
        Function<T, T> getNextPage
    ) {
        return StreamSupport.stream(new PaginationSpliterator<>(firstPage, haveNextPage, getNextPage), false);
    }
}


class PaginationSpliterator<T> implements Spliterator<T> {
    private T currentPage;
    private final Predicate<T> haveNextPage;
    private final Function<T, T> getNextPage;
    private boolean finished = false;

    public PaginationSpliterator(T firstPage, Predicate<T> haveNextPage, Function<T, T> getNextPage) {
        this.currentPage = firstPage;
        this.haveNextPage = haveNextPage;
        this.getNextPage = getNextPage;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (finished) {
            return false;
        }
        action.accept(currentPage);
        if (!haveNextPage.test(currentPage)) {
            finished = true;
        } else {
            currentPage = getNextPage.apply(currentPage);
        }
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null; // Not supporting parallel streams
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE; // Unknown size
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }
}
