package me.raska.opendvs.core.rest.filtering;

import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FilterableImpl implements Filterable {
    private final Map<String, String> filters;
    
    @Override
    public Map<String, String> getFilters() {
        return filters;
    }
    
}