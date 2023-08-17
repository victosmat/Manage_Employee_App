package com.example.demo.payLoad.mapper;

import com.example.demo.entity.*;
import com.example.demo.payLoad.dto.JobDetailsDTO;
import com.example.demo.payLoad.dto.StructureDTO;
import com.example.demo.payLoad.dto.UserDTO;
import com.example.demo.payLoad.dto.UserDTOInSession;
import com.example.demo.payLoad.request.AddressRequest;
import com.example.demo.payLoad.request.JobDetailsRequest;
import com.example.demo.payLoad.request.UserRegistryRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapperRequestToDTO implements IMapperRequestToDTO {
    private final ModelMapper modelMapper = new ModelMapper();

    public UserDTO mapperUserRegistryRequestToDTO(UserRegistryRequest userRegistryRequest) {
        return modelMapper.map(userRegistryRequest, UserDTO.class);
    }

    public StructureDTO mapperStructureToDTO(Structure structure) {
        return modelMapper.map(structure, StructureDTO.class);
    }

    public UserDTO mapperUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserID(user.getId());
        userDTO.setRole(user.getRoles()
                .stream()
                .map(role -> role.getNoteRole().name())
                .collect(Collectors.joining(", ")));
        userDTO.setUsername(user.getAccount().getUsername());
        userDTO.setFullName(user.getFullName());
        List<AddressRequest> addressRequests = new ArrayList<>();
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            addressRequests = user.getAddress()
                    .stream()
                    .map(address -> new AddressRequest(address.getStreet(), address.getCity()))
                    .collect(Collectors.toList());
        }
        userDTO.setAddress(addressRequests);

        userDTO.setEmail(user.getEmail());
        return userDTO;
    }

    public JobDetailsDTO mapperCronJobToDTO(JobDetailsRequest jobDetailsRequest) {
        return modelMapper.map(jobDetailsRequest, JobDetailsDTO.class);
    }

    public UserDTOInSession mapUserToUserDTOInSession(User user) {
        UserDTOInSession userDTOInSession = new UserDTOInSession();
        userDTOInSession.setUserId(user.getId());
        userDTOInSession.setFullName(user.getFullName());
        userDTOInSession.setEmail(user.getEmail());
        userDTOInSession.setRole(user.getRoles()
                .stream()
                .map(Role::getNoteRole)
                .map(Enum::toString)
                .collect(Collectors.joining(", ")));
        userDTOInSession.setUsername(user.getAccount().getUsername());
        return userDTOInSession;
    }

    public JobDetailsDTO mapperCronJobToDTO(JobDetails jobDetails) {
        JobDetailsDTO jobDetailsDTO = modelMapper.map(jobDetails, JobDetailsDTO.class);
        jobDetailsDTO.setUserIDs(jobDetails.getUsers()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList()));
        return jobDetailsDTO;
    }

    public List<AddressRequest> mapAddressToRequest(List<Address> addresses) {
        return addresses
                .stream()
                .map(address -> new AddressRequest(address.getStreet(), address.getCity()))
                .collect(Collectors.toList());
    }
}
