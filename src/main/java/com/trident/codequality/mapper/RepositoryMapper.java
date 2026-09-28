package com.trident.codequality.mapper;

import com.trident.codequality.domain.CodeRepository;
import com.trident.codequality.dto.repository.RepositoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {

    @Mapping(target = "registeredBy", source = "registeredBy.username")
    RepositoryResponse toResponse(CodeRepository repository);
}
