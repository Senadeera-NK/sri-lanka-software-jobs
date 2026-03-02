package lk.jobs.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lk.jobs.model.Job;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

//reads and write to the jobs.json
public class JsonStore {
    private final File file = new File("jobs.json");
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void save(List<Job> finalJobs){
        try{
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, finalJobs);
        }catch(Exception e){
            System.err.println("Failed to write to jobs.json: " + e.getMessage());
        }
    }

    public List<Job> load(){
        if(!file.exists() || file.length()==0)return new ArrayList<>();
        try{
            return mapper.readValue(file, new TypeReference<List<Job>>(){});
        }catch(Exception e){
            System.err.println("Could not read jobs.json, starting fresh.");
            return new ArrayList<>();
        }
    }
}
