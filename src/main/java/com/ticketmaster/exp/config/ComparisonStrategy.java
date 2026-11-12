package com.ticketmaster.exp.config;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ComparisonStrategy implements Serializable {
    private String type;
    private List<String> ignoreFields;
}
