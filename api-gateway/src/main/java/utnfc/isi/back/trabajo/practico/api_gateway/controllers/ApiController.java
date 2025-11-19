package utnfc.isi.back.trabajo.practico.api_gateway.controllers;


import org.slf4j.Logger;             
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-prueba")
public class ApiController {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    @GetMapping("")
    public String texto() {
        log.info("Endpoint /api-prueba invocado");
        return "HOLA MUNDO PRUEBA";
    }
    
}
