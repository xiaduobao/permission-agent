package com.permission.agent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.dto.LlmResponse;
import com.permission.agent.agent.dto.ToolCall;
import com.permission.agent.agent.tool.AgentToolRegistry;
import com.permission.agent.service.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MockAgentEngine {

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "(zhangsan|lisi|admin|张三|李四|[a-z][a-z0-9_]{2,})", Pattern.CASE_INSENSITIVE);

    private final AgentToolRegistry toolRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmResponse plan(AgentContext ctx, List<ChatMessage> messages, String userQuery) {
        String lower = userQuery.toLowerCase();
        LlmResponse response = new LlmResponse();

        if (containsAny(lower, "分配", "加上", "授予") && containsAny(lower, "角色")) {
            ToolCall call = buildAssignRolesCall(userQuery);
            if (call != null) {
                response.setToolCalls(List.of(call));
                return response;
            }
        }
        if (containsAny(lower, "风险", "巡检", "扫描")) {
            if (containsAny(lower, "触发", "执行", "开始", "扫描", "巡检")) {
                response.setToolCalls(List.of(buildCall("trigger_risk_scan", "{}")));
                return response;
            }
            response.setToolCalls(List.of(buildCall("list_pending_risks", "{}")));
            return response;
        }
        if (containsAny(lower, "推荐", "模板", "入职")) {
            String position = extractPosition(userQuery);
            response.setToolCalls(List.of(buildCall("recommend_role_template",
                    "{\"position\":\"" + position + "\"}")));
            return response;
        }
        if (containsAny(lower, "权限", "角色", "查询", "查看", "有哪些")) {
            String username = extractUsername(userQuery);
            ObjectNode args = objectMapper.createObjectNode();
            if (username != null) {
                args.put("username", username);
            }
            response.setToolCalls(List.of(buildCall("query_user_permission", args.toString())));
            return response;
        }

        response.setContent("【AI权限助手】\n您好，我是企业权限管理Agent。我可以帮您：\n" +
                "• 查询用户/角色权限（如：查询 zhangsan 的权限）\n" +
                "• 分析权限风险（如：列出待处理风险）\n" +
                "• 推荐权限配置（如：推荐研发工程师权限模板）\n" +
                "• 分配角色（如：给 zhangsan 分配研发角色）");
        return response;
    }

    public String summarizeToolResult(String toolName, String toolResult) {
        return switch (toolName) {
            case "query_user_permission" -> "【权限查询结果】\n" + toolResult;
            case "list_pending_risks" -> "【待处理风险】\n" + toolResult;
            case "trigger_risk_scan" -> "【巡检结果】\n" + toolResult;
            case "recommend_role_template" -> "【权限配置推荐】\n" + toolResult;
            case "assign_user_roles" -> "【角色分配结果】\n" + toolResult;
            default -> toolResult;
        };
    }

    private ToolCall buildCall(String name, String args) {
        ToolCall call = new ToolCall();
        call.setId(PendingActionService.newToolCallId());
        call.setName(name);
        call.setArguments(args);
        return call;
    }

    private ToolCall buildAssignRolesCall(String query) {
        String username = extractUsername(query);
        if (username == null) {
            username = "zhangsan";
        }
        List<String> roles = new ArrayList<>();
        if (query.contains("研发")) {
            roles.add("研发角色");
        }
        if (query.contains("普通")) {
            roles.add("普通用户");
        }
        if (roles.isEmpty()) {
            roles.add("研发角色");
        }
        try {
            ObjectNode args = objectMapper.createObjectNode();
            args.put("username", username);
            ArrayNode roleNames = args.putArray("role_names");
            roles.forEach(roleNames::add);
            return buildCall("assign_user_roles", args.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractUsername(String query) {
        if (query.contains("zhangsan") || query.contains("张三")) return "zhangsan";
        if (query.contains("lisi") || query.contains("李四")) return "lisi";
        if (query.contains("admin")) return "admin";
        Matcher m = USERNAME_PATTERN.matcher(query);
        if (m.find()) {
            String found = m.group(1);
            if (!found.equalsIgnoreCase("查询") && !found.equalsIgnoreCase("权限")) {
                return found.toLowerCase();
            }
        }
        return null;
    }

    private String extractPosition(String query) {
        if (query.contains("研发")) return "研发工程师";
        if (query.contains("产品")) return "产品经理";
        if (query.contains("管理")) return "管理员";
        return "";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
