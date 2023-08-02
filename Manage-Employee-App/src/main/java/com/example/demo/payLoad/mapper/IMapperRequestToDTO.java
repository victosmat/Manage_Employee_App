package com.example.demo.payLoad.mapper;

import com.example.demo.entity.Structure;
import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.JobDetailsDTO;
import com.example.demo.payLoad.dto.StructureDTO;
import com.example.demo.payLoad.dto.UserDTO;
import com.example.demo.payLoad.request.JobDetailsRequest;
import com.example.demo.payLoad.request.UserRegistryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.stream.Collectors;

@Mapper
public interface IMapperRequestToDTO {
    IMapperRequestToDTO INSTANCE = Mappers.getMapper(IMapperRequestToDTO.class);

    UserDTO mapperUserRegistryRequestToDTO(UserRegistryRequest userRegistryRequest);

    StructureDTO mapperStructureToDTO(Structure structure);

//    @Mapping(source = "ID", target = "userID")
//    @Mapping(source = "account.username", target = "username")
//    @Mapping(target = "role", expression = "java(mapRole(user))")
    UserDTO mapperUserToDTO(User user);

    default String mapRole(User user) {
        return user.getRoles()
                .stream()
                .map(role -> role.getNoteRole().name())
                .collect(Collectors.joining(", "));
    }

    JobDetailsDTO mapperCronJobToDTO(JobDetailsRequest jobDetailsRequest);
}
