package me.raska.opendvs.core.rest.filtering;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver to automatically populate Filterable class on REST endpoints.
 * 
 * @author raskaluk
 *
 */
public class FilterableArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String PREFIX = "filter.";

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType().equals(Filterable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final Map<String, String> filters = new HashMap<>();

        for (Entry<String, String[]> param : webRequest.getParameterMap().entrySet()) {
            if (param.getKey() != null && param.getValue() != null && param.getValue().length > 0
                    && param.getKey().startsWith(PREFIX)) {
                String key = param.getKey().substring(PREFIX.length());
                // ignore empty keys
                if (!key.isEmpty()) {
                    filters.put(key, param.getValue()[0]);
                }
            }
        }

        return new FilterableImpl(filters);
    }

}
