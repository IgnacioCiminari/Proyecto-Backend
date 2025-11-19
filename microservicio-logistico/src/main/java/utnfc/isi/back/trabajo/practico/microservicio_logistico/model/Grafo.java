package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.stereotype.Component;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.EnlacesService;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.NodosService;

@Component
public class Grafo {
    private List<Nodo> nodos;
    private List<Enlace> enlaces;
    private final NodosService nodosService;
    private final EnlacesService enlacesService;

    public Grafo(NodosService nodosService, EnlacesService enlacesService) {
        this.nodosService = nodosService;
        this.enlacesService = enlacesService;
    }

    public void agregarNodo(Nodo nodo) {
        nodos.add(nodo);
    }


    public List<List<Nodo>> encontrarCaminos(Double latOrigen, Double longOrigen, Double latDestino, Double longDestino, int cantidad) {
        // Cargo nodos y enlaces reales
        this.nodos = nodosService.obtenerTodos();
        this.enlaces = enlacesService.obtenerTodos();

        // Paso 1 — Seleccionar nodos REALES más cercanos
        Nodo inicio = this.buscarNodoMasCercano(latOrigen, longOrigen);
        Nodo destino = this.buscarNodoMasCercano(latDestino, longDestino);

        if (inicio == null || destino == null) {
            return List.of();
        }

        List<List<Nodo>> caminos = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {

            // Paso 2 — Resetear nodos antes de cada A*
            inicializarNodos();

            // Paso 3 — Ejecutar A*
            List<Nodo> camino = ejecutarAStar(inicio, destino);

            if (camino == null || camino.isEmpty()) {
                break; // no hay más caminos
            }

            caminos.add(camino);

            // Paso 4 — Penalizar enlaces usados para forzar rutas alternativas
            penalizarDistancias(camino);
        }

        return caminos;
    }

    private void inicializarNodos() {
        for (Nodo n : nodos) {
            n.setPadre(null);
            n.setG(Double.POSITIVE_INFINITY);
            n.setH(0.0);
            n.setF(Double.POSITIVE_INFINITY);
        }
    }

    private List<Nodo> ejecutarAStar(Nodo inicio, Nodo destino) {

        Set<Nodo> cerrados = new HashSet<>();
        PriorityQueue<Nodo> abiertos = new PriorityQueue<>(Comparator.comparingDouble(Nodo::getF));
    
        inicio.setG(0.0);
        inicio.setH(calcularHeuristica(inicio, destino));
        inicio.setF(inicio.getH());
        abiertos.add(inicio);
    
        while (!abiertos.isEmpty()) {
    
            Nodo actual = abiertos.poll();
    
            if (actual.equals(destino)) {
                return reconstruirCamino(actual);
            }
    
            cerrados.add(actual);
    
            List<Enlace> salientes = buscarEnlacesNodoOrigen(actual);
    
            for (Enlace enlace : salientes) {
                Nodo vecino = enlace.getDestino();
    
                if (cerrados.contains(vecino)) continue;
    
                double nuevoG = actual.getG() + enlace.getDistancia();
    
                boolean esMejor = nuevoG < vecino.getG();
    
                if (!abiertos.contains(vecino) || esMejor) {
                    vecino.setPadre(actual);
                    vecino.setG(nuevoG);
                    vecino.setH(calcularHeuristica(vecino, destino));
                    vecino.setF(vecino.getG() + vecino.getH());
    
                    if (!abiertos.contains(vecino)) {
                        abiertos.add(vecino);
                    }
                }
            }
        }
    
        return null; // no hay camino
    }

    private void penalizarDistancias(List<Nodo> camino) {
        double factor = 1.35; // 35% de penalización para forzar variantes
    
        for (int i = 0; i < camino.size() - 1; i++) {
            Nodo a = camino.get(i);
            Nodo b = camino.get(i + 1);
    
            for (Enlace e : enlaces) {
                if (e.getOrigen().equals(a) && e.getDestino().equals(b)) {
                    e.setDistancia(e.getDistancia() * factor);
                }
            }
        }
    }

    public Nodo buscarNodoMasCercano(Double latitud, Double longitud) {

        // Si no hay nodos cargados, no podemos hacer nada
        List<Nodo> todos = nodosService.obtenerTodos();
        if (todos == null || todos.isEmpty()) {
            return null;
        }
    
        Nodo mejor = null;
        double mejorDist = Double.POSITIVE_INFINITY;
    
        // Nodo "temporal" para calcular distancia
        Nodo pivote = Nodo.builder()
            .latitud(latitud)
            .longitud(longitud)
            .build();
    
        for (Nodo n : todos) {
    
            double dist = nodosService.calcularDistancia(pivote, n).kilometros();
    
            if (dist < mejorDist) {
                mejorDist = dist;
                mejor = n;
            }
        }
    
        return mejor;
    }
    

    public List<Enlace> buscarEnlacesNodoOrigen(Nodo nodo) {
        List<Enlace> enlacesNodo = new ArrayList<>();
        for (Enlace enlace : enlaces) {
            if (enlace.getOrigen().equals(nodo)) {
                enlacesNodo.add(enlace);
            }
        }
        return enlacesNodo;
    }

    /**
     * Calcula la heurística (distancia estimada) entre dos nodos.
     * En este caso usamos distancia euclidiana.
     */
    private Double calcularHeuristica(Nodo a, Nodo b) {
        return nodosService.calcularDistancia(a, b).kilometros();
    }

    /**
     * Reconstruye el camino desde el destino hasta el origen
     * utilizando los punteros 'padre' de cada nodo.
     */
    private List<Nodo> reconstruirCamino(Nodo destino) {
        List<Nodo> camino = new ArrayList<>();
        Nodo actual = destino;

        while (actual != null) {
            camino.add(actual);
            actual = actual.getPadre();
        }

        Collections.reverse(camino); // El camino se construye de atrás hacia adelante
        return camino;
    }
}