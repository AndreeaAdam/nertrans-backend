package ro.nertrans.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @Description Configuration Class
 * This overrides application.properties max fileSize
 *
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties
public class ConfigProperties {
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver commonsMultipartResolver(){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(134217728);
        multipartResolver.setMaxUploadSizePerFile(134217728);
        return multipartResolver;
    }
}
