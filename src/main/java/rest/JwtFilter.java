package rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import util.JwtUtil;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) {
        // Rutas públicas: registro y login no requieren token
        String path = ctx.getUriInfo().getPath();
        if (path.startsWith("usuarios/")) return;

        // Preflight CORS pasa sin validación
        if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) return;

        String authHeader = ctx.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Token requerido\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build());
            return;
        }

        try {
            JwtUtil.validarToken(authHeader.substring(7));
        } catch (Exception e) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Token inválido o expirado\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        }
    }
}
