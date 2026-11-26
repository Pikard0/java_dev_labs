package com.java.toystore.domain.mapper;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyResponse;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import com.java.toystore.domain.model.Toy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ToyMapper {

    ToyResponse toResponse(Toy toy);

    Toy toEntity(ToyCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ToyUpdateRequest request, @MappingTarget Toy toy);
}
