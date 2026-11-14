package common.extension;

import api.Config.Config;
import common.annotation.UserAgent;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserAgentExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(
            ExtensionContext context) {

        UserAgent annotation = context.getTestMethod()
                .map(method -> method.getAnnotation(UserAgent.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled(
                    "Ограничение к UserAgent отсутствует");
        }

        String currentUserAgent = Config.getProperty("userAgent");

        if (annotation.value().equalsIgnoreCase(currentUserAgent)) {
            return ConditionEvaluationResult.enabled(
                    "UserAgent соответствует настройкам конфигурации");
        }

        return ConditionEvaluationResult.disabled(
                "Тест пропущен, требуемый UserAgent: " + annotation.value()
                        + ",UserAgent в конфигурации: " + currentUserAgent);
    }
}