package com.ticketya.observability;

import java.util.List;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
public class ObservabilityAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletConfiguration implements WebMvcConfigurer {
        @Bean FilterRegistrationBean<ServletHttpLoggingFilter> httpLogging(
                @Value("${spring.application.name:unknown}") String service) {
            var registration = new FilterRegistrationBean<>(new ServletHttpLoggingFilter(service));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
            registration.setAsyncSupported(true);
            return registration;
        }
        @Override public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
            resolvers.addFirst((request, response, handler, error) -> {
                request.setAttribute(HttpEvents.ERROR_ATTRIBUTE, error.getClass().getSimpleName());
                return null; // Preserva los handlers y los contratos de errores existentes.
            });
        }
        @Bean static BeanPostProcessor correlationRestClients(@Value("${spring.application.name:unknown}") String service) {
            var interceptor = new CorrelationHttpInterceptor(service);
            return new BeanPostProcessor() {
                @Override public Object postProcessAfterInitialization(Object bean, String name) {
                    if (bean instanceof RestClient.Builder builder) builder.requestInterceptor(interceptor);
                    if (bean instanceof RestTemplate template) template.getInterceptors().add(interceptor);
                    return bean;
                }
            };
        }
    }
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveConfiguration {
        @Bean @Order(Ordered.HIGHEST_PRECEDENCE + 20)
        ReactiveHttpLoggingFilter reactiveHttpLogging(@Value("${spring.application.name:unknown}") String service) {
            return new ReactiveHttpLoggingFilter(service);
        }
    }
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    static class FeignConfiguration {
        @Bean feign.RequestInterceptor correlationFeignInterceptor() {
            return template -> { template.removeHeader(Correlation.HEADER); template.header(Correlation.HEADER, Correlation.currentOrNew()); };
        }
        @Bean feign.Capability correlationFeignCapability(@Value("${spring.application.name:unknown}") String service) {
            return new CorrelationFeignCapability(service);
        }
    }
}
