package utnfc.isi.back.trabajo.practico.api_gateway.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                // Verificacion de roles para acceder a los endpoints publicos.

                // ========== ENDPOINTS QUE PUEDEN ACCEDER LOS CLIENTES ==========
                // En teoria es para crear una solicitud
                .pathMatchers(HttpMethod.POST, "/comercial/solicitudes")
                    .hasAnyRole("CLIENTE", "ADMIN")
                
                // En teoria es para obtener una solicitud por ID
                .pathMatchers(HttpMethod.GET, "/comercial/solicitudes/{id}")
                    .hasAnyRole("CLIENTE", "ADMIN") // cliente podria ver cualquier solicitud?
                
                // En teoria es para obtener los todos contenedores de un cliente
                .pathMatchers(HttpMethod.GET, "/comercial/contenedores/cliente/{idCliente}")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Consultar estado del contenedor
                .pathMatchers(HttpMethod.GET, "/comercial/contenedores/{idContenedor}")
                    .hasAnyRole("CLIENTE", "ADMIN")
                
                // Seguimiento de una solicitud
                .pathMatchers(HttpMethod.GET, "/comercial/solicitudes/seguimiento/{idSolicitud}")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Crear un cliente
                .pathMatchers(HttpMethod.POST, "/comercial/clientes")
                    .hasAnyRole("CLIENTE", "ADMIN")

                // Obtener su cliente
                .pathMatchers(HttpMethod.GET, "/comercial/clientes/{idCliente}")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    
                // Darse de baja
                .pathMatchers(HttpMethod.DELETE, "/comercial/clientes/{idCliente}")
                    .hasAnyRole("CLIENTE", "ADMIN")
                
                // Modificarse un atributo
                .pathMatchers(HttpMethod.PUT, "/comercial/clientes/{idCliente}")
                    .hasAnyRole("CLIENTE", "ADMIN")
                
                // ========== ENDPOINTS QUE PUEDEN ACCEDER LOS TRANSPORTISTAS ==========
                // Permite al transportista obtener los camiones de un transportista (Camiones asignados a el enviando su ID)
                .pathMatchers(HttpMethod.GET, "/logistico/camiones/por-transportista/{idTransportista}")
                    .hasAnyRole("TRANSPORTISTA", "ADMIN")
                
                // Permite al Transportista iniciar un tramo
                .pathMatchers(HttpMethod.PUT, "/logistico/tramos/{idTramo}/iniciar")
                    .hasAnyRole("TRANSPORTISTA", "ADMIN")
                
                // Permite al Transportista finalizar un tramo
                .pathMatchers(HttpMethod.PUT, "/logistico/tramos/{idTramo}/finalizar")
                    .hasAnyRole("TRANSPORTISTA", "ADMIN")
                
                // Obtengo los tramos asignados al camion
                .pathMatchers(HttpMethod.GET, "/logistico/tramos/camion/{dominio}")
                    .hasAnyRole("TRANSPORTISTA", "ADMIN")
                
                // ========== ENDPOINTS QUE PUEDEN ACCEDER LOS ADMINS ==========
                // ========== Posiblemente eliminar ==========

                // ===========================================
                
                // Dar de baja un camion
                .pathMatchers(HttpMethod.DELETE, "/logistico/camiones/{dominio}")
                    .hasAnyRole("ADMIN")
                
                // ===== ABMC DE DEPÓSITOS =====
                // Get Depósito by ID
                .pathMatchers(HttpMethod.GET, "/logistico/depositos/{idDeposito}")
                    .hasAnyRole("ADMIN")
                
                // Get TODOS los depósitos
                .pathMatchers(HttpMethod.GET, "/logistico/depositos")
                    .hasAnyRole("ADMIN")
                
                // Crear UN Depósito
                .pathMatchers(HttpMethod.POST, "/logistico/depositos")
                    .hasAnyRole("ADMIN")
                
                // Crear VARIOS Depósitos
                .pathMatchers(HttpMethod.POST, "/logistico/depositos/varios")
                    .hasAnyRole("ADMIN")
                
                // Actualizar costo por estadia depósito
                .pathMatchers(HttpMethod.PUT, "/logistico/depositos/{idDeposito}/costo-estadia")
                    .hasAnyRole("ADMIN")
                
                // ===== ABMC DE TARIFAS =====
                // Get Tarifa by ID
                .pathMatchers(HttpMethod.GET, "/logistico/tarifas/{idTarifa}")
                    .hasAnyRole("ADMIN")
                
                // Get Tarifas disponibles
                .pathMatchers(HttpMethod.GET, "/logistico/tarifas/disponible")
                    .hasAnyRole("ADMIN")
                
                // Get TODAS Tarifas
                .pathMatchers(HttpMethod.GET, "/logistico/tarifas/todas")
                    .hasAnyRole("ADMIN")
                
                // Crear Tarifa
                .pathMatchers(HttpMethod.POST, "/logistico/tarifas")
                    .hasAnyRole("ADMIN")
                
                // Dar de baja Tarifa
                .pathMatchers(HttpMethod.DELETE, "/logistico/tarifas/{idTarifa}")
                    .hasAnyRole("ADMIN")
                
                // ===== ABMC DE CONTENEDORES =====
                // Get contenedor By ID
                .pathMatchers(HttpMethod.GET, "/comercial/contenedores/{idContenedor}")
                    .hasAnyRole("ADMIN")
                
                // ===================================
                
                // ===== TRAMOS =====
                // Asignar y remover camiones a tramos
                .pathMatchers(HttpMethod.PUT, "/logistico/tramos/{idTramo}/asignar-camion/{dominio}")
                    .hasAnyRole("ADMIN")
                
                .pathMatchers(HttpMethod.PUT, "/logistico/tramos/{idTramo}/quitar-camion")
                    .hasAnyRole("ADMIN")
                
                // Get tramo By ID
                .pathMatchers(HttpMethod.GET, "/logistico/tramos/{idTramo}")
                    .hasAnyRole("ADMIN")
                
                // Get tramos By Ruta
                .pathMatchers(HttpMethod.GET, "/logistico/tramos/ruta/{idRuta}")
                    .hasAnyRole("ADMIN")
                
                // ===== RUTAS =====
                // Get Ruta By ID
                .pathMatchers(HttpMethod.GET, "/logistico/rutas/{idRuta}")
                    .hasAnyRole("ADMIN")
                
                // Get Rutas pendientes
                .pathMatchers(HttpMethod.GET, "/logistico/rutas/pendientes")
                    .hasAnyRole("ADMIN")

                // Consultar rutas tentativas
                .pathMatchers(HttpMethod.GET, "/logistico/rutas/alternativas/{idRuta}")
                    .hasAnyRole("ADMIN")
                
                // Crear tramos para rutas
                .pathMatchers(HttpMethod.POST, "/logistico/rutas/crear/{idSolicitud}/tramos")
                    .hasAnyRole("ADMIN")
                
                // Eliminar ruta
                .pathMatchers(HttpMethod.DELETE, "/logistico/rutas/{idRuta}")
                    .hasAnyRole("ADMIN")
                
                // ===== ABMC PARA TRANSPORTISTA =====
                // Get Transportista by ID
                .pathMatchers(HttpMethod.GET, "/logistico/transportistas/{idTransportista}")
                    .hasAnyRole("ADMIN")
                
                // Get TODOS Transportista
                .pathMatchers(HttpMethod.GET, "/logistico/transportistas/todos")
                    .hasAnyRole("ADMIN")
                
                // Crear Transportista
                .pathMatchers(HttpMethod.POST, "/logistico/transportistas")
                    .hasAnyRole("ADMIN")
                
                // Modificar Transportista
                .pathMatchers(HttpMethod.PUT, "/logistico/transportistas/{idTransportista}")
                    .hasAnyRole("ADMIN")
                
                // Eliminar Transportista
                .pathMatchers(HttpMethod.DELETE, "/logistico/transportistas/{idTransportista}")
                    .hasAnyRole("ADMIN")
                
                .pathMatchers("/comercial/**") 
                    .hasAnyRole("ADMIN")
                .pathMatchers("/logistico/**")
                    .hasAnyRole("ADMIN")

                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<JwtAuthenticationToken>> jwtAuthenticationConverter() {

        return new Converter<Jwt, Mono<JwtAuthenticationToken>>() {

            @Override
            public Mono<JwtAuthenticationToken> convert(Jwt jwt) {

                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                List<String> roles = realmAccess != null
                        ? (List<String>) realmAccess.get("roles")
                        : List.of();

                List<GrantedAuthority> authorities = roles.stream()
                        .map(r -> "ROLE_" + r.toUpperCase())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                return Mono.just(new JwtAuthenticationToken(jwt, authorities));
            }
        };
    }

}
