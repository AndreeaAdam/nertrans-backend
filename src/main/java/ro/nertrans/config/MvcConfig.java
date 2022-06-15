package ro.nertrans.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${resource_location}")
    private String resourceLocation;

    @Value("${resource_handler}")
    private String resourceHandler;


    /**
     * @param registry -
     * @Description: These are basically the static folders we to give spring access to
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceHandler).addResourceLocations(resourceLocation, "classpath:/static/");
    }
}