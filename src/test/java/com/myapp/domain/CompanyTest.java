package com.myapp.domain;

import static com.myapp.domain.CompanyTestSamples.*;
import static com.myapp.domain.PersonTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Company.class);
        Company company1 = getCompanySample1();
        Company company2 = new Company();
        assertThat(company1).isNotEqualTo(company2);

        company2.setId(company1.getId());
        assertThat(company1).isEqualTo(company2);

        company2 = getCompanySample2();
        assertThat(company1).isNotEqualTo(company2);
    }

    @Test
    void personTest() throws Exception {
        Company company = getCompanyRandomSampleGenerator();
        Person personBack = getPersonRandomSampleGenerator();

        company.addPerson(personBack);
        assertThat(company.getPeople()).containsOnly(personBack);
        assertThat(personBack.getCompany()).isEqualTo(company);

        company.removePerson(personBack);
        assertThat(company.getPeople()).doesNotContain(personBack);
        assertThat(personBack.getCompany()).isNull();

        company.people(new HashSet<>(Set.of(personBack)));
        assertThat(company.getPeople()).containsOnly(personBack);
        assertThat(personBack.getCompany()).isEqualTo(company);

        company.setPeople(new HashSet<>());
        assertThat(company.getPeople()).doesNotContain(personBack);
        assertThat(personBack.getCompany()).isNull();
    }
}
