package com.ibm.api.psd2.api.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter 
{
	
	//http://localhost:8081/oauth2server/oauth/check_token
	@Value("${oauth.checktoken.endpoint}")
	private String checkTokenEndpoint;
	
	@Value("${oauth.client.id}")
	private String clientId;

	@Value("${oauth.client.secret}")
	private String clientSecret;

	@Bean
	public FilterRegistrationBean corsFilter() {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("*");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("OPTIONS");
	    config.addAllowedMethod("HEAD");
	    config.addAllowedMethod("GET");
	    config.addAllowedMethod("PUT");
	    config.addAllowedMethod("POST");
	    config.addAllowedMethod("DELETE");
	    config.addAllowedMethod("PATCH");
	    source.registerCorsConfiguration("/**", config);
	    
	    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
	    bean.setOrder(-100);
	    return bean;
	}	
	
	@Bean
	public ResourceServerTokenServices resourceServerTokenServices()
	{
		RemoteTokenServices rts = new RemoteTokenServices();
		rts.setClientId(clientId);
		rts.setClientSecret(clientSecret);
		rts.setCheckTokenEndpointUrl(checkTokenEndpoint);
		return rts;
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception
	{
		resources.tokenServices(resourceServerTokenServices());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
		http.authorizeRequests().antMatchers("/webjars/**", "/swagger-ui.html", "/swagger-resources", "/v2/api-docs", "/admin/**").permitAll();
		http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().authorizeRequests().anyRequest().authenticated();
//		http.antMatcher("/user/**").authorizeRequests().anyRequest().access("#oauth2.hasScope('read')");
	}
}
