package com.trident.codequality.service.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trident.codequality.config.WebClientConfig;
import com.trident.codequality.config.properties.AiProperties;
import com.trident.codequality.exception.ExternalServiceException;

import lombok.extern.slf4j.Slf4j;
import com.trident.codequality.service.github.SourceFile;
@Slf4j
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

	private static final String SYSTEM_PROMPT = """
			You are a senior static-analysis engine. Analyze the supplied source file and
			respond with a SINGLE JSON object and nothing else, matching exactly:
			{
			  "issues": [{"lineNumber": int|null, "severity": "INFO|LOW|MEDIUM|HIGH|CRITICAL",
			              "category": string, "title": string, "description": string, "suggestion": string}],
			  "tests": [{"testFilePath": string, "framework": string, "testCode": string, "description": string}],
			  "quality": {"maintainability": 0-100, "reliability": 0-100, "security": 0-100, "coverageEstimate": 0-100},
			  "compliance": [{"standard": string, "rule": string,
			                  "status": "PASS|WARN|FAIL|NOT_APPLICABLE", "details": string}]
			}
			Detect real bugs, security flaws and code smells. Generate at least one meaningful
			unit test for the primary unit. Keep testCode compilable for the file's language.""";

	private final WebClient ai;
	private final AiProperties props;
	private final ObjectMapper objectMapper;

	public AiAnalysisServiceImpl(@Qualifier(WebClientConfig.AI_CLIENT) WebClient ai, AiProperties props,
			ObjectMapper objectMapper) {
		this.ai = ai;
		this.props = props;
		this.objectMapper = objectMapper;
	}

	@Override
	public AiAnalysisResult analyzeFile(SourceFile file) {
		if (!StringUtils.hasText(props.apiKey())) {
			throw new ExternalServiceException("AI provider is not configured (missing ai.api-key)");
		}
		Map<String, Object> body = Map.of("model", props.model(), "temperature", 0.1, "response_format",
				Map.of("type", "json_object"), "messages", List.of(Map.of("role", "system", "content", SYSTEM_PROMPT),
						Map.of("role", "user", "content", userPrompt(file))));
		try {
			JsonNode response = ai.post().uri(chatUri()).bodyValue(body).retrieve().bodyToMono(JsonNode.class).block();

			String content = extractContent(response);
			return objectMapper.readValue(content, AiAnalysisResult.class);
		} catch (WebClientResponseException ex) {
			throw new ExternalServiceException(
					"AI request failed (%d): %s".formatted(ex.getStatusCode().value(), ex.getResponseBodyAsString()),
					ex);
		} catch (ExternalServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ExternalServiceException("Failed to parse AI response: " + ex.getMessage(), ex);
		}
	}

	private String userPrompt(SourceFile file) {
		return "File path: %s%n%n```%n%s%n```".formatted(file.path(), file.content());
	}

	private String chatUri() {
		if (props.isAzure()) {
			return "/openai/deployments/%s/chat/completions?api-version=%s".formatted(props.azureDeployment(),
					props.azureApiVersion());
		}
		return "/chat/completions";
	}

	private String extractContent(JsonNode response) {
		if (response == null) {
			throw new ExternalServiceException("Empty AI response");
		}
		JsonNode content = response.path("choices").path(0).path("message").path("content");
		if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
			throw new ExternalServiceException("AI response contained no content");
		}
		return content.asText();
	}
}
