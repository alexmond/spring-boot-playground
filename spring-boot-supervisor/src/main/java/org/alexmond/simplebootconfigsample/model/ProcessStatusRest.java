package org.alexmond.simplebootconfigsample.model;

import lombok.Data;
import org.alexmond.simplebootconfigsample.config.ProcessConfig;

/**
 * Represents a user entity in the system.
 * Implements Identifiable interface for consistent ID handling.
 */
@Data
public class ProcessStatusRest {

    /**
     * Current status of the process (e.g., running, stopped, failed)
     */
    private ProcessStatus status;
    /**
     * Process ID assigned by the operating system
     */
    private Long pid;

    /**
     * Timestamp when the process was started
     */
    private Long startTime;
    /**
     * Configuration settings for the process
     */
    private ProcessConfig processConfig;

    public ProcessStatusRest(RunningProcess runningProcess) {
        if (runningProcess.getProcess() != null) {
            pid = runningProcess.getProcess().pid();
        }
        status = runningProcess.getProcessStatus();
    }
}
