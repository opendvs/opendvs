package me.raska.opendvs.core.rest.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FilterableSpecification {
    /**
     * Builds proper Predicate criteria for managed entities.
     * 
     * @param class1
     * @param filterable
     *            Filter map
     * @param whereMap
     *            WHERE conditions, filters will be stripped of these keys
     * @param inMap
     *            IN conditions
     * @return JPA 2 Specification
     */
    public <T> Specification<T> handleEntityFiltering(Class<T> cls, Filterable filterable, Map<String, Object> whereMap,
            Map<String, Set<String>> inMap) {
        return (root, query, cb) -> {
            List<Predicate> wheres = new ArrayList<>();

            // sanitize likes
            Map<String, String> filters = new HashMap<>(filterable.getFilters());
            if (whereMap != null) {
                filters.keySet().removeAll(whereMap.keySet());
            }

            // likes
            for (Entry<String, String> filter : filters.entrySet()) {
                wheres.add(cb.like(root.get(filter.getKey()), filter.getValue()));
            }

            // wheres
            if (whereMap != null) {
                for (Entry<String, Object> where : whereMap.entrySet()) {
                    wheres.add(cb.equal(root.get(where.getKey()), where.getValue()));
                }
            }

            // ins
            if (inMap != null) {
                for (Entry<String, Set<String>> in : inMap.entrySet()) {
                    wheres.add(root.get(in.getKey()).in(in.getValue()));
                }
            }
            return cb.and(wheres.toArray(new Predicate[] {}));
        };
    }

    public <T> Specification<T> handleEntityFiltering(Class<T> cls, Filterable filterable) {
        return handleEntityFiltering(cls, filterable, null, null);
    }

    public <T> Specification<T> handleEntityFiltering(Class<T> cls, Filterable filterable,
            Map<String, Object> whereMap) {
        return handleEntityFiltering(cls, filterable, whereMap, null);
    }
}
