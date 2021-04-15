package com.brchain.common.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration

public class SwaggerConfiguration {
	@Bean
	public OpenAPI springShopOpenAPI() {

		SecurityScheme      securityScheme    = new SecurityScheme().type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");
		SecurityRequirement schemaRequirement = new SecurityRequirement().addList("JWT Auth");

		return new OpenAPI().components(new Components().addSecuritySchemes("JWT Auth", securityScheme))
			.security(Arrays.asList(schemaRequirement))
			.info(new Info().title("BR_Chain API")
				.description("brchain(fabric web admin) application")
				.version("v1.0.0")
				.license(new License().name("Apache 2.0")
					.url("http://springdoc.org")))
			.externalDocs(new ExternalDocumentation().description("git")
				.url("https://github.com/jaehyuen/BR_Chain_Admin_Server"));
	}
//    @Bean
//    public Docket api() {
//    	
//        return new Docket(DocumentationType.SWAGGER_2)
//        		.apiInfo(this.getApiInfo()).select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.any())
//                .build().useDefaultResponseMessages(true).securitySchemes(Arrays.asList(apiKey()));
//                
//    }
//
//    private ApiInfo getApiInfo() {
//        return new ApiInfoBuilder()
//                .title("BRChain API")
//                .version("1.0")
//                .description("BaRak block chain admin server API")
//                .build();
//    }
//    
//    private ApiKey apiKey() {
//        return new ApiKey("Authorization", "Authorization", "header");
//    }
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
}