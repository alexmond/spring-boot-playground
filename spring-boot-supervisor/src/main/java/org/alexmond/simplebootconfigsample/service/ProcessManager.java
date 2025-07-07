package org.alexmond.simplebootconfigsample.service;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.simplebootconfigsample.config.ProcessConfig;
import org.alexmond.simplebootconfigsample.config.SupervisorConfig;
import org.alexmond.simplebootconfigsample.model.RunningProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProcessManager {
    private final SupervisorConfig supervisorConfig;
    private final Map<String, CompletableFuture<Void>> processFutures = new HashMap<>();
    Map<String, RunningProcess> allProcesses;

    @Autowired
    public ProcessManager(SupervisorConfig supervisorConfig) {
        this.supervisorConfig = supervisorConfig;
        this.allProcesses = supervisorConfig.getProcess().stream()
                .collect(Collectors.toMap(ProcessConfig::getName, RunningProcess::new));
    }

    public void startAllProcess() throws IOException {
        allProcesses.keySet().forEach(name -> {
            try {
                startProcess(name);
            } catch (IOException e) {
                log.error("Failed to start process: {}", name, e);
            }
        });
    }

    public RunningProcess startProcessSimple() throws IOException {
        // Assuming you have a default process config
        String defaultProcessName = allProcesses.keySet().iterator().next();
        startProcess(defaultProcessName);
        return allProcesses.get(defaultProcessName);
    }

    private void startProcess(String name) throws IOException {
        ProcessConfig processConfig = allProcesses.get(name).getProcessConfig();
        if (processConfig == null) {
            throw new IllegalArgumentException("No process config found for: " + name);
        }

        try {
            List<String> command = new ArrayList<>();
            command.add(processConfig.getCommand());
            command.addAll(processConfig.getArgs());
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(processConfig.getWorkingDirectory()));

            // Handle output redirection
            if (processConfig.getRedirectErrorStream()) {
                processBuilder.redirectErrorStream(true);
            } else {
                processBuilder.redirectError(
                        ProcessBuilder.Redirect.appendTo(new File(processConfig.getStderrLogfile())));
            }
            processBuilder.redirectOutput(
                    ProcessBuilder.Redirect.appendTo(new File(processConfig.getStdoutLogfile())));
            
            Map<String, String> environment = processBuilder.environment();
            environment.putAll(processConfig.getEnv());

            // Start the process
            Process proc = processBuilder.start();
            
            // Record start time
            LocalDateTime startTime = LocalDateTime.now();
            allProcesses.get(name).setStartTime(startTime);
            
            // Store process references
            allProcesses.get(name).setProcess(proc);
            allProcesses.get(name).setProcess(proc);
            
            log.info("Process '{}' started with PID: {} at {}", name, proc.pid(), startTime);
            
            // Monitor process completion asynchronously using Spring's thread pool
            CompletableFuture<Void> future = monitorProcessCompletion(name, proc, startTime);
            processFutures.put(name, future);
            
        } catch (IOException e) {
            log.error("Failed to start process: {}", name, e);
            throw new RuntimeException("Failed to start process: " + name, e);
        }
    }

    @Async
    public CompletableFuture<Void> monitorProcessCompletion(String name, Process proc, LocalDateTime startTime) {
        try {
            int exitCode = proc.waitFor();
            LocalDateTime endTime = LocalDateTime.now();
            allProcesses.get(name).setEndTime(endTime);
            
            Duration duration = Duration.between(startTime, endTime);
            log.info("Process '{}' ended with exit code: {} after running for: {}", 
                    name, exitCode, allProcesses.get(name).formatDuration(duration));
            
            // Clean up
            allProcesses.get(name).setProcess(null);
            processFutures.remove(name);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Process monitoring interrupted for: {}", name);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    public void stopProcess(String processName) {
        Process process = allProcesses.get(processName).getProcess();
        if (process != null) {
            log.info("Stopping process: {}", processName);
            
            // Try graceful shutdown first
            process.destroy();
            
            try {
                // Wait for graceful shutdown (5 seconds timeout)
                boolean exited = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    log.warn("Process {} did not exit gracefully, force killing...", processName);
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for process to stop: {}", processName);
            }

                allProcesses.get(processName).setEndTime(LocalDateTime.now());
            
            // Clean up
            allProcesses.get(processName).setProcess(null);
            processFutures.remove(processName);
        }
    }



}
