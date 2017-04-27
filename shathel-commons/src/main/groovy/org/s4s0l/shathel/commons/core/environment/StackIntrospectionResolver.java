package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.DefaultGlobalEnricherProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class StackIntrospectionResolver {
    private final List<Map<String, String>> map;

    public StackIntrospectionResolver(List<Map<String, String>> map) {
        this.map = map;
    }

    public List<Map<String, String>> getMap() {
        return map;
    }

    public String getGa() {
        List<String> collect = map.stream()
                .map(x -> x.get(DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GA))
                .distinct().collect(Collectors.toList());
        if (collect.size() != 1) {
            throw new RuntimeException("Existing stack is not consistent org.shathel.stack.ga for all services should have same value!");
        }
        return collect.get(0);
    }

    public String getGav() {
        List<String> collect = map.stream()
                .map(x -> x.get(DefaultGlobalEnricherProvider.LABEL_SHATHEL_STACK_GAV))
                .distinct().collect(Collectors.toList());
        if (collect.size() != 1) {
            throw new RuntimeException("Existing stack is not consistent org.shathel.stack.gav for all services should have same value!");
        }
        return collect.get(0);
    }

    public Map<String,Long> getLabelValues(String labelName){
        return map.stream()
                .map(x -> x.get(labelName))
                .collect(Collectors.groupingBy(p -> p,
                        Collectors.counting()));
    }



    public  Map<String, String> getShathelLabels() {
        List<Map<String, String>> collect = map.stream().map(x ->
                x.entrySet()
                        .stream()
                        .filter(e -> e.getKey().startsWith(DefaultGlobalEnricherProvider.LABEL_PREFIX_SHATHEL))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ).distinct()
                .collect(Collectors.toList());
        if(collect.size()!=1){
            throw new RuntimeException(DefaultGlobalEnricherProvider.LABEL_PREFIX_SHATHEL + " labels inconsistent");
        }
        return collect.get(0);
    }
}
