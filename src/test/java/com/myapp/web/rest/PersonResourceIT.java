package com.myapp.web.rest;

import static com.myapp.domain.PersonAsserts.*;
import static com.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.IntegrationTest;
import com.myapp.domain.Person;
import com.myapp.repository.PersonRepository;
import com.myapp.service.dto.PersonDTO;
import com.myapp.service.mapper.PersonMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link PersonResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PersonResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/people";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private MockMvc restPersonMockMvc;

    private Person person;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Person createEntity() {
        Person person = new Person().name(DEFAULT_NAME);
        return person;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Person createUpdatedEntity() {
        Person person = new Person().name(UPDATED_NAME);
        return person;
    }

    @BeforeEach
    public void initTest() {
        personRepository.deleteAll();
        person = createEntity();
    }

    @Test
    void createPerson() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);
        var returnedPersonDTO = om.readValue(
            restPersonMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(personDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PersonDTO.class
        );

        // Validate the Person in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPerson = personMapper.toEntity(returnedPersonDTO);
        assertPersonUpdatableFieldsEquals(returnedPerson, getPersistedPerson(returnedPerson));
    }

    @Test
    void createPersonWithExistingId() throws Exception {
        // Create the Person with an existing ID
        person.setId(1L);
        PersonDTO personDTO = personMapper.toDto(person);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPersonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(personDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllPeople() throws Exception {
        // Initialize the database
        personRepository.save(person);

        // Get all the personList
        restPersonMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(person.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    void getPerson() throws Exception {
        // Initialize the database
        personRepository.save(person);

        // Get the person
        restPersonMockMvc
            .perform(get(ENTITY_API_URL_ID, person.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(person.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    void getNonExistingPerson() throws Exception {
        // Get the person
        restPersonMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingPerson() throws Exception {
        // Initialize the database
        personRepository.save(person);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the person
        Person updatedPerson = personRepository.findById(person.getId()).orElseThrow();
        updatedPerson.name(UPDATED_NAME);
        PersonDTO personDTO = personMapper.toDto(updatedPerson);

        restPersonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, personDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(personDTO))
            )
            .andExpect(status().isOk());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPersonToMatchAllProperties(updatedPerson);
    }

    @Test
    void putNonExistingPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, personDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(personDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(personDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(personDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePersonWithPatch() throws Exception {
        // Initialize the database
        personRepository.save(person);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the person using partial update
        Person partialUpdatedPerson = new Person();
        partialUpdatedPerson.setId(person.getId());

        restPersonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPerson.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPerson))
            )
            .andExpect(status().isOk());

        // Validate the Person in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersonUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPerson, person), getPersistedPerson(person));
    }

    @Test
    void fullUpdatePersonWithPatch() throws Exception {
        // Initialize the database
        personRepository.save(person);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the person using partial update
        Person partialUpdatedPerson = new Person();
        partialUpdatedPerson.setId(person.getId());

        partialUpdatedPerson.name(UPDATED_NAME);

        restPersonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPerson.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPerson))
            )
            .andExpect(status().isOk());

        // Validate the Person in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersonUpdatableFieldsEquals(partialUpdatedPerson, getPersistedPerson(partialUpdatedPerson));
    }

    @Test
    void patchNonExistingPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, personDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(personDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(personDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPerson() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        person.setId(longCount.incrementAndGet());

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPersonMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(personDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Person in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePerson() throws Exception {
        // Initialize the database
        personRepository.save(person);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the person
        restPersonMockMvc
            .perform(delete(ENTITY_API_URL_ID, person.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return personRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Person getPersistedPerson(Person person) {
        return personRepository.findById(person.getId()).orElseThrow();
    }

    protected void assertPersistedPersonToMatchAllProperties(Person expectedPerson) {
        assertPersonAllPropertiesEquals(expectedPerson, getPersistedPerson(expectedPerson));
    }

    protected void assertPersistedPersonToMatchUpdatableProperties(Person expectedPerson) {
        assertPersonAllUpdatablePropertiesEquals(expectedPerson, getPersistedPerson(expectedPerson));
    }
}
