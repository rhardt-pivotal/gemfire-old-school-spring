package io.pivotal.pa.rhardt.gemfire.smgwsa.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "io.pivotal.pa.rhardt.gemfire.smgwsa.web" })
public class ServletConfig {


}
