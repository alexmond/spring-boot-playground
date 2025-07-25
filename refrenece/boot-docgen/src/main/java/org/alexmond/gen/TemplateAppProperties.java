package org.alexmond.gen;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateAppProperties {

    String name;
    String title;
    String heading;
    String id;
    String order;
    List<AppProperty> properties = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class AppProperty {
        String name;
        Object defaultValue;
        String description;
    }
}
