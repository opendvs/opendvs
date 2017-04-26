package me.raska.opendvs.core.configuration;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import me.raska.opendvs.core.rest.filtering.FilterableArgumentResolver;

/**
 * Autoconfiguration to properly register FilterableArgumentResolver;
 *
 * @author raskaluk
 *
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@ConditionalOnClass({ FilterableArgumentResolver.class })
public class FilterableResolverAutoconfiguration {

    @Bean
    public FilterableArgumentResolver filterableArgumentResolver() {
        return new FilterableArgumentResolver();
    }

    @Configuration
    @Order(0)
    protected static class FilterableMvcConfiguration extends WebMvcConfigurerAdapter {
        private final FilterableArgumentResolver filterableResolver;

        protected FilterableMvcConfiguration(FilterableArgumentResolver filterableHandlerMethodArgumentResolver) {
            this.filterableResolver = filterableHandlerMethodArgumentResolver;
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(this.filterableResolver);
        }
    }

}
