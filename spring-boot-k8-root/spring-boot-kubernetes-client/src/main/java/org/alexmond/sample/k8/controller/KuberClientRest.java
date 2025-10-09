package org.alexmond.sample.k8.controller;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class KuberClientRest {


    private final ApiClient apiClient;

    private CoreV1Api api() {
        return new CoreV1Api(apiClient);
    }

    @GetMapping("/pods")
    public ResponseEntity<List<String>> getPods() {
        try {
            V1PodList podList = api().listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null);
            List<String> pods = podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pods);
        } catch (ApiException e) {
            log.error("Failed to get pods", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pods/{namespace}")
    public ResponseEntity<List<String>> getPodsByNamespace(@PathVariable String namespace) {
        try {
            V1PodList podList = api().listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null, null);
            List<String> pods = podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pods);
        } catch (ApiException e) {
            log.error("Failed to get pods in namespace: {}", namespace, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pods/details/{name}")
    public ResponseEntity<V1Pod> getPodByName(@PathVariable String name) {
        try {
            V1Pod pod = api().listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null)
                    .getItems().stream()
                    .filter(p -> name.equals(p.getMetadata().getName()))
                    .findFirst()
                    .orElse(null);
            return pod != null ? ResponseEntity.ok(pod) : ResponseEntity.notFound().build();
        } catch (ApiException e) {
            log.error("Failed to get pod: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pods/count")
    public ResponseEntity<Integer> getPodCount() {
        try {
            int count = api().listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null)
                    .getItems()
                    .size();
            return ResponseEntity.ok(count);
        } catch (ApiException e) {
            log.error("Failed to get pod count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/namespaces")
    public ResponseEntity<List<String>> getNamespaces() {
        try {
            V1NamespaceList namespaceList = api().listNamespace(null, null, null, null, null, null, null, null, null, null, null);
            List<String> namespaces = namespaceList.getItems().stream()
                    .map(namespace -> namespace.getMetadata().getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(namespaces);
        } catch (ApiException e) {
            log.error("Failed to get namespaces", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
