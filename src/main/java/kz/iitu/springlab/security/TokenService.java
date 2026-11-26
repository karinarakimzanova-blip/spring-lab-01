package kz.iitu.springlab.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "books:read",
            "books:write",
            "admin:read"
    );

    private final AppUserRepository userRepository;

    public TokenService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String issueToken(String username, String scope) {
        if (!ALLOWED_SCOPES.contains(scope)) {
            throw new IllegalArgumentException("Unknown scope: " + scope);
        }

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!user.isEnabled()) {
            throw new IllegalStateException("User is disabled: " + username);
        }

        String header = encodeBase64("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        List<String> roles = List.copyOf(user.getRoles());
        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"scope\":\"%s\",\"roles\":%s,\"exp\":%d}",
                escapeJson(user.getUsername()),
                escapeJson(scope),
                toJsonArray(roles),
                exp
        );
        String payload = encodeBase64(payloadJson);
        return header + "." + payload + ".unsigned";
    }

    public Map<String, Object> decodeClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid token format");
        }
        String decodedPayload = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
        );
        return parseSimpleJsonObject(decodedPayload);
    }

    private String encodeBase64(String raw) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Map<String, Object> parseSimpleJsonObject(String json) {
        Map<String, Object> claims = new LinkedHashMap<>();
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("Malformed token payload");
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return claims;
        }
        List<String> pairs = splitTopLevel(body);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) {
                continue;
            }
            String key = unquote(pair.substring(0, colonIndex).trim());
            String rawVal = pair.substring(colonIndex + 1).trim();
            if (rawVal.startsWith("\"") && rawVal.endsWith("\"")) {
                claims.put(key, unquote(rawVal));
            } else if (rawVal.startsWith("[") && rawVal.endsWith("]")) {
                String inner = rawVal.substring(1, rawVal.length() - 1).trim();
                if (inner.isEmpty()) {
                    claims.put(key, List.of());
                } else {
                    claims.put(key, Arrays.stream(inner.split(","))
                            .map(String::trim)
                            .map(this::unquote)
                            .toList());
                }
            } else {
                try {
                    claims.put(key, Long.parseLong(rawVal));
                } catch (NumberFormatException ex) {
                    claims.put(key, rawVal);
                }
            }
        }
        return claims;
    }

    private List<String> splitTopLevel(String body) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        boolean escaped = false;
        int bracketDepth = 0;
        int start = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes) {
                if (c == '[') {
                    bracketDepth++;
                } else if (c == ']') {
                    bracketDepth--;
                } else if (c == ',' && bracketDepth == 0) {
                    result.add(body.substring(start, i));
                    start = i + 1;
                }
            }
        }
        result.add(body.substring(start));
        return result;
    }

    private String unquote(String s) {
        String t = s.trim();
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            return t.substring(1, t.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return t;
    }
}