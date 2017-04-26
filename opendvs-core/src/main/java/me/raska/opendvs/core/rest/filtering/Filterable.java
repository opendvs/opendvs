package me.raska.opendvs.core.rest.filtering;

import java.util.Map;

/**
 * Filter arguments captor to be used for all Page responses. 
 * 
 * @author raskaluk
 *
 */
@FunctionalInterface
public interface Filterable {
    Map<String, String> getFilters();
}