package com.myapp.domain;

import static com.myapp.domain.CompanyTestSamples.*;
import static com.myapp.domain.PersonTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Person.class);
        Person person1 = getPersonSample1();
        Person person2 = new Person();
        assertThat(person1).isNotEqualTo(person2);

        person2.setId(person1.getId());
        assertThat(person1).isEqualTo(person2);

        person2 = getPersonSample2();
        assertThat(person1).isNotEqualTo(person2);
    }

    @Test
    void companyTest() throws Exception {
        Person person = getPersonRandomSampleGenerator();
        Company companyBack = getCompanyRandomSampleGenerator();

        person.setCompany(companyBack);
        assertThat(person.getCompany()).isEqualTo(companyBack);

        person.company(null);
        assertThat(person.getCompany()).isNull();
    }
}
