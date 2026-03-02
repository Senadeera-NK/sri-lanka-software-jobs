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

    public void saveAndMerge(List<Job> newJobs){
        List<Job> existingJobs = load();
        //merge using map for deduplication (based on your ID method)
        Map<String, Job> allJobsMap = new HashMap<>();
        existingJobs.forEach(j -> allJobsMap.put(j.id(), j));
        newJobs.forEach(j->allJobsMap.put(j.id(),j));

        //filter and sort (newest first)
        List<Job> finalJobs = allJobsMap.values().stream()
                .filter(j->j.datePosted().isAfter(LocalDate.now().minusDays(14)))
                .sorted(Comparator.comparing(Job::datePosted).reversed())
                .collect(Collectors.toList());

        try{
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, finalJobs);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<Job> load(){
        if(!file.exists())return new ArrayList<>();
        try{
            return mapper.readValue(file, new TypeReference<List<Job>>(){});
        }catch(Exception e){
            return new ArrayList<>();
        }
    }
}
