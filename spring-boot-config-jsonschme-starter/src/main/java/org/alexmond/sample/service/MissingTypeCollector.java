package org.alexmond.sample.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MissingTypeCollector {
    Map<String, Integer> missingTypes = new HashMap<>();

    public void addType(String type) {
        missingTypes.merge(type, 1, Integer::sum);
    }

    public List<Map.Entry<String,Integer>> getMissingTypes() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(missingTypes.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list;
    }
}
