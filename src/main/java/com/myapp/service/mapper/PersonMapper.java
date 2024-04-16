package com.myapp.service.mapper;

import com.myapp.domain.Company;
import com.myapp.domain.Person;
import com.myapp.service.dto.CompanyDTO;
import com.myapp.service.dto.PersonDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Person} and its DTO {@link PersonDTO}.
 */
@Mapper(componentModel = "spring")
public interface PersonMapper extends EntityMapper<PersonDTO, Person> {
    @Mapping(target = "company", source = "company", qualifiedByName = "companyId")
    PersonDTO toDto(Person s);

    @Named("companyId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CompanyDTO toDtoCompanyId(Company company);
}
