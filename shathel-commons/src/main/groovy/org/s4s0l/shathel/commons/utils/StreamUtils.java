package org.s4s0l.shathel.commons.utils;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * http://bendra.github.io/java/lambda/stream/2014/11/01/fun-with-java-8-streams-ii.html
 *
 * @author Marcin Wielgus
 */
public class StreamUtils {

    /**
     * Stream elements in reverse-index order
     *
     * @param input ordered list to be reversed
     * @param <T>   type in stream
     * @return a stream of the elements in reverse
     */
    public static <T> Stream<T> streamInReverse(List<T> input) {
        if (input instanceof LinkedList<?>) {
            return streamInReverse((LinkedList<T>) input);
        }
        return IntStream.range(1, input.size() + 1).mapToObj(
                i -> input.get(input.size() - i));
    }

    /**
     * Stream elements in reverse-index order
     *
     * @param input
     * @return a stream of the elements in reverse
     */
    private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
        Iterator<T> descendingIterator = input.descendingIterator();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                descendingIterator, Spliterator.ORDERED), false);
    }
}
