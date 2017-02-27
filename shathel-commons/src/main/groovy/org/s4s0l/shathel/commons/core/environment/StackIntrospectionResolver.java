package org.s4s0l.shathel.commons.core.environment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class StackIntrospectionResolver {
    private final List<Map<String, String>> map;

    public StackIntrospectionResolver(List<Map<String, String>> map) {
        this.map = map;
    }

    public String getGa() {
        List<String> collect = map.stream()
                .map(x -> x.get("org.shathel.stack.ga"))
                .distinct().collect(Collectors.toList());
        if (collect.size() != 1) {
            throw new RuntimeException("Existing stack is not consistent org.shathel.stack.ga for all services should have same value!");
        }
        return collect.get(0);
    }

    public String getGav() {
        List<String> collect = map.stream()
                .map(x -> x.get("org.shathel.stack.gav"))
                .distinct().collect(Collectors.toList());
        if (collect.size() != 1) {
            throw new RuntimeException("Existing stack is not consistent org.shathel.stack.gav for all services should have same value!");
        }
        return collect.get(0);
    }

    public  Map<String, String> getShathelLabels() {
        List<Map<String, String>> collect = map.stream().map(x ->
                x.entrySet()
                        .stream()
                        .filter(e -> e.getKey().startsWith("org.shathel.stack"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ).distinct()
                .collect(Collectors.toList());
        if(collect.size()!=1){
            throw new RuntimeException("org.shathel.stack labels inconsistent");
        }
        return collect.get(0);
    }
}
