package org.alexmond.sample.k8.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing Kubernetes resources using the Fabric8 Kubernetes client.
 * Provides endpoints for retrieving information about pods and namespaces.
 *
 * @author Alex Mondshain
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class Fabric8ClientRest {
    
    private final KubernetesClient kubernetesClient;


    /**
     * Retrieves a list of all pod names across all namespaces.
     *
     * @return ResponseEntity containing a list of pod names if successful, or an error response
     */
    @GetMapping("/pods")
    public ResponseEntity<List<String>> getPods() {
        try {
            List<String> pods = kubernetesClient.pods().inAnyNamespace().list().getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            log.error("Failed to get pods", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a list of pod names in the specified namespace.
     *
     * @param namespace the Kubernetes namespace to query
     * @return ResponseEntity containing a list of pod names if successful, or an error response
     */
    @GetMapping("/pods/{namespace}")
    public ResponseEntity<List<String>> getPodsByNamespace(@PathVariable String namespace) {
        try {
            List<String> pods = kubernetesClient.pods().inNamespace(namespace).list().getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            log.error("Failed to get pods in namespace: {}", namespace, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves detailed information about a specific pod.
     *
     * @param name the name of the pod to retrieve
     * @return ResponseEntity containing the Pod object if found, or a not found response
     */
    @GetMapping("/pods/details/{name}")
    public ResponseEntity<Pod> getPodByName(@PathVariable String name) {
        try {
            Pod pod = kubernetesClient.pods().inAnyNamespace().list().getItems().stream()
                    .filter(p -> name.equals(p.getMetadata().getName()))
                    .findFirst()
                    .orElse(null);
            return pod != null ? ResponseEntity.ok(pod) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get pod: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Counts the total number of pods across all namespaces.
     *
     * @return ResponseEntity containing the total pod count if successful, or an error response
     */
    @GetMapping("/pods/count")
    public ResponseEntity<Integer> getPodCount() {
        try {
            int count = kubernetesClient.pods().inAnyNamespace().list().getItems().size();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get pod count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a list of all namespace names in the cluster.
     *
     * @return ResponseEntity containing a list of namespace names if successful, or an error response
     */
    @GetMapping("/namespaces")
    public ResponseEntity<List<String>> getNamespaces() {
        try {
            List<String> namespaces = kubernetesClient.namespaces().list().getItems().stream()
                    .map(namespace -> namespace.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(namespaces);
        } catch (Exception e) {
            log.error("Failed to get namespaces", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
