package org.alexmond.gen;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TemplateProperties {

    List<TemplateAppProperties> apps = new ArrayList<>();
    private String version;

    public String getEffectiveDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
        Date date = new Date();
        return "Effective Date: " + formatter.format(date);
    }

}
