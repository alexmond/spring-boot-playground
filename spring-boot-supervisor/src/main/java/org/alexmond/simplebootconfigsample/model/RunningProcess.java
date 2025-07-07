package org.alexmond.simplebootconfigsample.model;

import lombok.Data;
import lombok.experimental.Delegate;
import org.alexmond.simplebootconfigsample.config.ProcessConfig;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a user entity in the system.
 * Implements Identifiable interface for consistent ID handling.
 */
@Data
public class RunningProcess {

    public RunningProcess(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

    @Delegate private ProcessConfig processConfig;
    private Process process = null;
    LocalDateTime startTime;
    LocalDateTime endTime;

    public boolean isProcessRunning(String processName) {
        return process != null && process.isAlive();
    }

    public ProcessStatus getProcessStatus() {
        if (process == null && startTime == null) {
            return ProcessStatus.NOT_STARTED;
        } else if (process != null && process.isAlive()) {
            return ProcessStatus.RUNNING;
        } else if (endTime != null) {
            return ProcessStatus.FINISHED;
        } else {
            return ProcessStatus.UNKNOWN;
        }
    }

    public Duration getProcessRuntime(String processName) {
        if (startTime == null) {
            return Duration.ZERO;
        }

        if (endTime != null) {
            // Process has ended
            return Duration.between(startTime, endTime);
        } else if (isProcessRunning(processName)) {
            // Process is still running
            return Duration.between(startTime, LocalDateTime.now());
        } else {
            // Process ended but no end time recorded (shouldn't happen normally)
            return Duration.between(startTime, LocalDateTime.now());
        }
    }

    public String getProcessRuntimeFormatted(String processName) {
        Duration runtime = getProcessRuntime(processName);
        return formatDuration(runtime);
    }


    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        if (hours > 0) {
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        } else if (minutes > 0) {
            return String.format("%02d:%02d.%03d", minutes, seconds, millis);
        } else {
            return String.format("%02d.%03d seconds", seconds, millis);
        }
    }
}