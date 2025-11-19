package utnfc.isi.back.trabajo.practico.microservicio_comercial.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;


@Component
public class ClienteSecurity {

    public Long getIdClienteToken() {
        Jwt jwt = (Jwt) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

        // Claim llega como String → se convierte a Long
        return Long.valueOf(jwt.getClaimAsString("id_cliente"));
    }

    public void validarId(Long idClientePath) {
        Long idClienteToken = getIdClienteToken();

        if (!idClienteToken.equals(idClientePath)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }
}


