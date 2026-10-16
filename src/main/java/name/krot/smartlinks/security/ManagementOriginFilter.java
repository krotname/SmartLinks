package name.krot.smartlinks.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** The edge overwrites the proxy headers; the application still rejects other origins. */
public class ManagementOriginFilter extends OncePerRequestFilter {
    private final String host;

    public ManagementOriginFilter(String host) {
        this.host = host;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        if (!host.equalsIgnoreCase(request.getServerName())) {
            response.sendError(404);
        } else if (!request.isSecure()) {
            response.sendError(403);
        } else {
            chain.doFilter(request, response);
        }
    }
}
