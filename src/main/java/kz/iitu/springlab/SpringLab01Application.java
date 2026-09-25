package kz.iitu.springlab;

import kz.iitu.springlab.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(AppProperties.class)
public class SpringLab01Application {
	public static void main(String[] args) {
		System.setProperty("spring.classformat.ignore", "true");
		SpringApplication.run(SpringLab01Application.class, args);
	}
}
