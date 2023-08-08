package com.example.demo.service;

import com.example.demo.entity.CheckTimeRequest;
import com.example.demo.entity.Structure;
import com.example.demo.entity.Time;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.IStatisticalNorDTO;
import com.example.demo.payLoad.dto.StatisticalErrorDTO;
import com.example.demo.payLoad.dto.StructureDTO;
import com.example.demo.repository.CheckTimeRequestRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.TimeRepository;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StructureService {
    @Autowired
    private StructureRepository structureRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MapperRequestToDTO mapperReToDTO;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private CheckTimeRequestRepository checkTimeRequestRepository;
    @Autowired
    private TimeRepository timeRepository;


    public Message<StructureDTO> checkTime(Long userID) {
        LocalDateTime dateTimeNow = LocalDateTime.now();
        String dateNow = dateTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime timeNow = LocalTime.parse(dateTimeNow.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        List<Structure> checkTimes = structureRepository.checkStructureInUser(dateNow, userID);
        User user = userRepository.findById(userID).orElse(null);
        assert user != null;
        LocalTime checkIn = LocalTime.parse(user.getTime().getCheckIn());
        LocalTime checkOut = LocalTime.parse(user.getTime().getCheckOut());
        if (checkTimes.size() == 1 && checkTimes.get(0).getStatus() == Structure.Status.CHECK_IN_MISSING) {
            Structure structure1 = checkTimes.get(0);
            if (timeNow.isAfter(checkIn)) structure1.setStatus(Structure.Status.CHECK_IN_LATE);
            else structure1.setStatus(Structure.Status.CHECK_IN);
            structureRepository.save(structure1);
            return new Message<>("successful check in!", HttpStatus.OK, mapperReToDTO.mapperStructureToDTO(structure1));
        } else if (checkTimes.size() == 1 && (checkTimes.get(0).getStatus() == Structure.Status.CHECK_IN || checkTimes.get(0).getStatus() == Structure.Status.CHECK_IN_LATE)) {
            Structure structure = new Structure(null, user, null, String.valueOf(dateTimeNow));
            if (timeNow.isBefore(checkOut)) structure.setStatus(Structure.Status.CHECK_OUT_EARLY);
            else structure.setStatus(Structure.Status.CHECK_OUT);
            structureRepository.save(structure);
            return new Message<>("successful check out!", HttpStatus.OK, mapperReToDTO.mapperStructureToDTO(structure));
        } else {
            Structure structure1 = checkTimes.get(1);
            structure1.setDateTime(String.valueOf(dateTimeNow));
            if (timeNow.isBefore(checkOut)) structure1.setStatus(Structure.Status.CHECK_OUT_EARLY);
            else structure1.setStatus(Structure.Status.CHECK_OUT);
            structure1 = structureRepository.save(structure1);
            return new Message<>("successful check out!", HttpStatus.OK, mapperReToDTO.mapperStructureToDTO(structure1));
        }
    }

    public Message<List<IStatisticalNorDTO>> getAllStructures(String from, String to) {
        List<IStatisticalNorDTO> statisticalNorDTOS = new ArrayList<>();
        if (from == null || to == null)
            statisticalNorDTOS = structureRepository.getAllStructures();
        else statisticalNorDTOS = structureRepository.getAllStructuresByDate(from, to);
        if (statisticalNorDTOS.isEmpty()) return new Message<>("no data!", HttpStatus.NOT_FOUND, null);
        else return new Message<>("successful!", HttpStatus.OK, statisticalNorDTOS);
    }

    public Message<List<StatisticalErrorDTO>> getAllStructuresError(int month, int year) {
        String firstDateOfMonth = String.valueOf(LocalDate.of(year, month, 1));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // Đặt ngày của Calendar thành ngày đầu tiên của tháng
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // Lấy số ngày cuối cùng của tháng
        String lateDateOfMonth = String.valueOf(LocalDate.of(year, month, lastDay));

        List<?> objects = structureRepository.getAllStructuresErrorByMonth(firstDateOfMonth, lateDateOfMonth);
        List<StatisticalErrorDTO> statisticalErrorDTOS = objects.stream()
                .map(o -> {
                    Object[] tuple = (Object[]) o;
                    StatisticalErrorDTO statisticalErrorDTO = new StatisticalErrorDTO();
                    statisticalErrorDTO.setUserID(Long.parseLong(tuple[0].toString()));
                    statisticalErrorDTO.setCheckInAt((String) tuple[1]);
                    statisticalErrorDTO.setCheckOutAt((String) tuple[2]);
                    statisticalErrorDTO.setStatusError((String) tuple[3]);
                    return statisticalErrorDTO;
                })
                .collect(Collectors.toList());

        if (statisticalErrorDTOS.isEmpty()) return new Message<>("no data!", HttpStatus.NOT_FOUND, null);
        else return new Message<>("successful!", HttpStatus.OK, statisticalErrorDTOS);
    }

    public Message<List<StatisticalErrorDTO>> getAllStructuresErrorByUser(int month, int year, Long userID) {
        String firstDateOfMonth = String.valueOf(LocalDate.of(year, month, 1));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // Đặt ngày của Calendar thành ngày đầu tiên của tháng
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // Lấy số ngày cuối cùng của tháng
        String lateDateOfMonth = String.valueOf(LocalDate.of(year, month, lastDay));

        List<?> objects = structureRepository.getAllStructuresErrorByMonthByUser(firstDateOfMonth, lateDateOfMonth, userID);
        List<StatisticalErrorDTO> statisticalErrorDTOS = objects.stream()
                .map(o -> {
                    Object[] tuple = (Object[]) o;
                    StatisticalErrorDTO statisticalErrorDTO = new StatisticalErrorDTO();
                    statisticalErrorDTO.setUserID(Long.parseLong(tuple[0].toString()));
                    statisticalErrorDTO.setCheckInAt((String) tuple[1]);
                    statisticalErrorDTO.setCheckOutAt((String) tuple[2]);
                    statisticalErrorDTO.setStatusError((String) tuple[3]);
                    return statisticalErrorDTO;
                })
                .collect(Collectors.toList());

        if (statisticalErrorDTOS.isEmpty()) return new Message<>("no data!", HttpStatus.NOT_FOUND, null);
        else return new Message<>("successful!", HttpStatus.OK, statisticalErrorDTOS);
    }

    public Message<List<IStatisticalNorDTO>> getAllStructuresByUser(String from, String to, String token) {
        Long userID = tokenProvider.getUserIdFromJWT(token);
        List<IStatisticalNorDTO> statisticalNorDTOS = new ArrayList<>();
        if (from == null || to == null)
            statisticalNorDTOS = structureRepository.getAllStructuresByUser(from, to, userID);
        else statisticalNorDTOS = structureRepository.getAllStructuresByUser(from, to, userID);
        if (statisticalNorDTOS.isEmpty()) return new Message<>("no data!", HttpStatus.NOT_FOUND, null);
        else return new Message<>("successful!", HttpStatus.OK, statisticalNorDTOS);
    }

    public Message<CheckTimeRequest> updateCheckTime(Long userID) {
        CheckTimeRequest checkTimeRequest = checkTimeRequestRepository.findByUserID(userID);
        if (checkTimeRequest == null) return new Message<>("no data check time!", HttpStatus.NOT_FOUND, null);
        else if (checkTimeRequest.isAccept())
            return new Message<>("request has been accepted!", HttpStatus.BAD_REQUEST, checkTimeRequest);
        else {
            checkTimeRequest.setAccept(true);
            checkTimeRequest = checkTimeRequestRepository.save(checkTimeRequest);
            Time time = userRepository.findById(userID).get().getTime();
            time.setCheckIn(checkTimeRequest.getCheckIn());
            time.setCheckOut(checkTimeRequest.getCheckOut());
            timeRepository.save(time);
            return new Message<>("Update time successful!", HttpStatus.OK, checkTimeRequest);
        }
    }
}
