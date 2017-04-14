package org.s4s0l.shathel.commons.utils;

import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Marcin Wielgus
 */
@Builder
public class ExtensionContext {
    @Singular
    private final List<ExtensionInterface> extensions;

    ExtensionContext(List<ExtensionInterface> extensions) {
        this.extensions = extensions;

        //Ugly but will work for now until i get rid of lombok
        //or figure out how to do sth after build is called
        for (ExtensionInterface extension : extensions) {
            extension.setExtensionContext(this);
        }
    }

    public <T extends ExtensionInterface> Optional<T> lookupOne(Class<T> clazz) {
        return (Optional<T>) StreamUtils.streamInReverse(extensions)
                .filter(it -> clazz.isAssignableFrom(it.getClass()))
                .findFirst();
    }


    public <T extends ExtensionInterface> Optional<T> lookupOneMatching(Class<T> clazz, Predicate<T> p) {
        return (Optional<T>) StreamUtils.streamInReverse(extensions)
                .filter(it -> clazz.isAssignableFrom(it.getClass()))
                .filter(x -> p.test((T) x))
                .findFirst();
    }

    public <T extends ExtensionInterface> Stream<T> lookupAll(Class<T> clazz) {
        return (Stream<T>) StreamUtils.streamInReverse(extensions)
                .filter(it -> clazz.isAssignableFrom(it.getClass()));
    }

}
