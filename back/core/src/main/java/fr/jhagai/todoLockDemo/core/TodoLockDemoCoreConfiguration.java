package fr.jhagai.todoLockDemo.core;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "fr.jhagai.todoLockDemo.core")
public class TodoLockDemoCoreConfiguration {
}
