package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootConfigMeta {
    private List<Group> groups;
    private List<Property> properties;
    private List<Hint> hints;
    private Ignored ignored;

    public BootConfigMeta() {
        this.groups = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.hints = new ArrayList<>();
        this.ignored = new Ignored();
    }

}
