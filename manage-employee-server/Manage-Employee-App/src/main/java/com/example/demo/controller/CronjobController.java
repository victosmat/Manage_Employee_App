package com.example.demo.controller;

import com.example.demo.entity.JobDetails;
import com.example.demo.component.MailComponent;
import com.example.demo.entity.User;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.JobDetailsDTO;
import com.example.demo.payLoad.request.JobDetailsRequest;
import com.example.demo.repository.JobDetailsRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledFuture;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/cronjob")
@Slf4j
public class CronjobController {
    private final Map<String, ScheduledFuture<?>> jobMaps = new HashMap<>();
    @Autowired
    private JobDetailsRepository jobDetailsRepository;
    private final TaskScheduler taskScheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private MapperRequestToDTO mapperRequestToDTO;

    public CronjobController(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void initAllJobs() {
        log.info("Start assigning work to employees !!!");
        List<JobDetails> jobDetailsList = jobDetailsRepository.findAllJobDetails(Sort.by("jobCode"));
        for (JobDetails jobDetails : jobDetailsList) {
            try {
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> {
                    List<User> userList = jobDetails.getUsers();
                    for (User user : userList) {
                        String content = "notice job: " + jobDetails.getDescription();
                        String subject = "Notice job";
                        MailComponent mailComponent = new MailComponent(user.getEmail(), subject, content);
                        emailService.sendMail(mailComponent);
                    }
                }, new CronTrigger(jobDetails.getCronTime()));
                jobMaps.put(jobDetails.getJobCode(), scheduledTask);
            } catch (IllegalArgumentException e) {
                // Xử lý ngoại lệ khi biểu thức cron không đúng định dạng
                log.error("Failed to handle job with id " + jobDetails.getId() + ": " + e.getMessage());
            }
        }
    }

    @PostMapping("/addJob")
    @Transactional
    public Message<JobDetailsDTO> addJob(@RequestBody JobDetailsRequest jobDetailsRequest) {
        Message<JobDetailsDTO> message = new Message<>();
        JobDetails jobDetailsCheck = jobDetailsRepository.findByJobCode(jobDetailsRequest.getJobCode());
        if (jobDetailsCheck != null) {
            String messageError = "Job code " + jobDetailsRequest.getJobCode() + " already exists";
            message = new Message<>(messageError, HttpStatus.INTERNAL_SERVER_ERROR, null);
            return message;
        } else
            try {
                JobDetails jobDetails = convertJobDetailsRequestToEntity(jobDetailsRequest);
                jobDetails = jobDetailsRepository.save(jobDetails);
                JobDetails finalJobDetails = jobDetails;
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> {
                    if (finalJobDetails.isJobExecuted()) return;
                    List<Long> userIds = finalJobDetails.getUsers().stream().map(User::getID).toList();
                    userIds.forEach(userID -> {
                        User user = userRepository.findById(userID).orElse(null);
                        if (user == null) log.error("User with id " + userID + " not found");
                        else {
                            String content = "notice job: " + jobDetailsRequest.getDescription();
                            String subject = "Notice job";
                            MailComponent mailComponent = new MailComponent(user.getEmail(), subject, content);
                            emailService.sendMail(mailComponent);
                            finalJobDetails.setJobExecuted(true);
                            jobDetailsRepository.save(finalJobDetails);
                        }
                    });
                }, new CronTrigger(jobDetailsRequest.getCronTime()));
                jobMaps.put(jobDetailsRequest.getJobCode(), scheduledTask);
                String messageSuccess = "Add job success";
                message = new Message<>(messageSuccess, HttpStatus.OK, mapperRequestToDTO.mapperCronJobToDTO(jobDetailsRequest));
            } catch (IllegalArgumentException e) {
                // Xử lý ngoại lệ khi biểu thức cron không đúng định dạng
                log.error("Failed to handle job with id " + jobDetailsRequest.getJobCode() + ": " + e.getMessage());
                String messageError = "Failed to handle job with id " + jobDetailsRequest.getJobCode() + ": " + e.getMessage();
                message = new Message<>(messageError, HttpStatus.INTERNAL_SERVER_ERROR, null);
            }
        return message;
    }

    @PutMapping("/updateJob")
    @Transactional
    public Message<JobDetailsDTO> updateJob(@RequestBody JobDetailsRequest jobDetailsRequest) {
        JobDetails jobDetails = convertJobDetailsRequestToEntity(jobDetailsRequest);
        jobDetailsRepository.save(jobDetails);
        JobDetails jobDetailsSave = jobDetailsRepository.save(jobDetails);
        Message<JobDetailsDTO> message = new Message<>();
        try {
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> {
                List<User> userList = jobDetails.getUsers();
                for (User user : userList) {
                    String content = "notice job: " + jobDetailsSave.getDescription();
                    String subject = "Notice job";
                    MailComponent mailComponent = new MailComponent(user.getEmail(), subject, content);
                    emailService.sendMail(mailComponent);
                }
            }, new CronTrigger(jobDetails.getCronTime()));
            String messageRes = "update job success";
            if (jobMaps.containsKey(jobDetailsSave.getJobCode())) {
                ScheduledFuture<?> scheduledTaskOld = jobMaps.get(jobDetailsSave.getJobCode());
                scheduledTaskOld.cancel(true);
                messageRes += " and job" + jobDetailsSave.getJobCode() + " cancel";
            }
            jobMaps.put(jobDetailsSave.getJobCode(), scheduledTask);
            message = new Message<>(messageRes, HttpStatus.OK, mapperRequestToDTO.mapperCronJobToDTO(jobDetailsRequest));
        } catch (IllegalArgumentException e) {
            // Xử lý ngoại lệ khi biểu thức cron không đúng định dạng
            log.error("Failed to handle job with id " + jobDetails.getId() + ": " + e.getMessage());
            String messageError = "Failed to handle job with id " + jobDetails.getId() + ": " + e.getMessage();
            message = new Message<>(messageError, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
        return message;
    }

    @DeleteMapping("/deleteJob/{jobCode}")
    @Transactional
    public Message<String> deleteJob(@PathVariable String jobCode) {
        Message<String> message = new Message<>();
        try {
            if (jobMaps.containsKey(jobCode)) {
                ScheduledFuture<?> scheduledTask = jobMaps.get(jobCode);
                scheduledTask.cancel(true);
                jobMaps.remove(jobCode);
            }
            JobDetails jobDetails = jobDetailsRepository.findByJobCode(jobCode);
            jobDetails.getUsers().removeAll(jobDetails.getUsers());
            jobDetailsRepository.delete(jobDetails);
            String messageSuccess = "Delete job " + jobCode + " success";
            message = new Message<>(messageSuccess, HttpStatus.OK, jobCode);
        } catch (IllegalArgumentException e) {
            // Xử lý ngoại lệ khi biểu thức cron không đúng định dạng
            log.error("Failed to handle job with id " + jobCode + ": " + e.getMessage());
            String messageError = "Failed to handle job with id " + jobCode + ": " + e.getMessage();
            message = new Message<>(messageError, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
        return message;
    }

    @GetMapping("/getAllJob")
    public Message<List<JobDetailsDTO>> getAllJob() {
        List<JobDetails> jobDetailsList = jobDetailsRepository.findAllJobDetails(Sort.by("jobCode"));
        List<JobDetailsDTO> jobDetailsDTOList = new ArrayList<>();
        jobDetailsList.forEach(jobDetails -> {
            jobDetailsDTOList.add(mapperRequestToDTO.mapperCronJobToDTO(jobDetails));
        });
        return new Message<>("Get all job success", HttpStatus.OK, jobDetailsDTOList);
    }

    @GetMapping("/getJobByUser/{userID}")
    public Message<List<JobDetailsDTO>> getJobByUser(@PathVariable Long userID) {
        List<JobDetails> jobDetailsList = jobDetailsRepository.findAllJobDetailsByUser(userID);
        List<JobDetailsDTO> jobDetailsDTOList = new ArrayList<>();
        jobDetailsList.forEach(jobDetails -> {
            JobDetailsDTO jobDetailsDTO = mapperRequestToDTO.mapperCronJobToDTO(jobDetails);
            jobDetailsDTO.setUserIDs(Collections.singletonList(userID));
            jobDetailsDTOList.add(jobDetailsDTO);
        });
        return new Message<>("Get all job success", HttpStatus.OK, jobDetailsDTOList);
    }

    public JobDetails convertJobDetailsRequestToEntity(JobDetailsRequest jobDetailsRequest) {
        List<User> userList = new ArrayList<>();
        jobDetailsRequest.getUserIDs().forEach(id -> {
            userList.add(userRepository.findById(id).orElse(null));
        });
        return new JobDetails(null,
                jobDetailsRequest.getJobCode(),
                jobDetailsRequest.getName(),
                jobDetailsRequest.getCronTime(),
                jobDetailsRequest.getDate(),
                jobDetailsRequest.getDescription(),
                userList,
                false);
    }
}
