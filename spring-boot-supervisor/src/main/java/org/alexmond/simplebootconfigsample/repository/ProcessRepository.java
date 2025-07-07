package org.alexmond.simplebootconfigsample.repository;

import org.alexmond.simplebootconfigsample.model.RunningProcess;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProcessRepository {

    List<RunningProcess> runningProcesses = new ArrayList<>();

    public List<RunningProcess> findAll(){
        return runningProcesses;
    }

    public RunningProcess addProcess(RunningProcess runningProcess){
        runningProcesses.add(runningProcess);
        return runningProcess;
    }

    public Optional<RunningProcess> updateProcess(int id, String newName){
        Optional<RunningProcess> findUser =  runningProcesses.stream()
                .filter(runningProcess -> runningProcess.getId()==id)
                .findFirst();
        findUser.ifPresent(runningProcess -> runningProcess.setName(newName));
        return findUser;
    }

    public boolean deleteProcess(int id){
        return runningProcesses.removeIf(runningProcess -> runningProcess.getId() == id);
    }


}
