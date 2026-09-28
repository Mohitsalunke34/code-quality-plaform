package com.trident.codequality.config;

import com.trident.codequality.config.properties.AiProperties;
import com.trident.codequality.config.properties.GitHubProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    public static final String GITHUB_CLIENT = "githubWebClient";
    public static final String AI_CLIENT = "aiWebClient";

    @Bean(GITHUB_CLIENT)
    public WebClient githubWebClient(GitHubProperties props) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(props.apiBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (StringUtils.hasText(props.token())) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.token());
        }
        return builder.build();
    }

    @Bean(AI_CLIENT)
    public WebClient aiWebClient(AiProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.timeoutSeconds() * 1000)
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(props.timeoutSeconds(), TimeUnit.SECONDS)));

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(props.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(props.apiKey())) {
            if (props.isAzure()) {
                builder.defaultHeader("api-key", props.apiKey());
            } else {
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey());
            }
        }
        return builder.build();
    }
}
