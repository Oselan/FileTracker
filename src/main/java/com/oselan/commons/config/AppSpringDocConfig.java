package com.oselan.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
 
// @Profile(value = {"local","dev"}) 
@Configuration 
public class AppSpringDocConfig {
   
	@Bean("AppSpringDocFwdFilter") 
	ForwardedHeaderFilter forwardedHeaderFilter() {
		ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
		filter.setRelativeRedirects(true);
		return filter;
	}
	
	 
    @Bean
    OpenAPI customizeOpenAPI() {
//      final String securitySchemeName = "bearerAuth";
      return new OpenAPI()
              .info(new Info().title("File Tracker API")
              .description("File Tracker Sample application")
              .version("v2.0.0")
              .license(new License().name("Apache 2.0").url("http://springdoc.org")))
//              .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
//              .components(new Components() //security scheme
//                  .addSecuritySchemes(securitySchemeName,// CommonAuthProperties.getInstance().getAuthorizationHeaderKey()
//                  new SecurityScheme().name(securitySchemeName)
//                                      .type(SecurityScheme.Type.HTTP)
//                                       .scheme("bearer")
//                                      .bearerFormat("JWT").in(In.HEADER))) 
              ;
//              .externalDocs(new ExternalDocumentation()
//              .description("Municipality Survey Documentation")
//              .url("https://springshop.wiki.github.org/docs"))     
  }
     
}
