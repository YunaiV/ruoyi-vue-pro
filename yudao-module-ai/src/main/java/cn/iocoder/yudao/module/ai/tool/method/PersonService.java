package cn.iocoder.yudao.module.ai.tool.method;

import java.util.List;
import java.util.Optional;

/**
 * 来自 Spring AI 官方文档
 *
 * Service interface for managing Person data.
 * Defines the contract for CRUD operations and search/filter functionalities.
 */
public interface PersonService {

    /**
     * Creates a new Person record.
     * Assigns a unique ID to the person and stores it.
     *
     * @param personData The data for the new person (ID field is ignored). Must not be null.
     * @return The created Person record, including the generated ID.
     */
    Person createPerson(Person personData);

    /**
     * Retrieves a Person by their unique ID.
     *
     * @param id The ID of the person to retrieve.
     * @return An Optional containing the found Person, or an empty Optional if not found.
     */
    Optional<Person> getPersonById(int id);

    /**
     * Retrieves all Person records currently stored.
     *
     * @return An unmodifiable List containing all Persons. Returns an empty list if none exist.
     */
    List<Person> getAllPersons();

    /**
     * Updates an existing Person record identified by ID.
     * Replaces the existing data with the provided data, keeping the original ID.
     *
     * @param id                The ID of the person to update.
     * @param updatedPersonData The new data for the person (ID field is ignored). Must not be null.
     * @return true if the person was found and updated, false otherwise.
     */
    boolean updatePerson(int id, Person updatedPersonData);

    /**
     * Deletes a Person record identified by ID.
     *
     * @param id The ID of the person to delete.
     * @return true if the person was found and deleted, false otherwise.
     */
    boolean deletePerson(int id);

    /**
     * Searches for Persons whose job title contains the given query string (case-insensitive).
     *
     * @param jobTitleQuery The string to search for within job titles. Can be null or blank.
     * @return An unmodifiable List of matching Persons. Returns an empty list if no matches or query is invalid.
     */
    List<Person> searchByJobTitle(String jobTitleQuery);

    /**
     * Filters Persons by their exact sex (case-insensitive).
     *
     * @param sex The sex to filter by (e.g., "Male", "Female"). Can be null or blank.
     * @return An unmodifiable List of matching Persons. Returns an empty list if no matches or filter is invalid.
     */
    List<Person> filterBySex(String sex);

    /**
     * Filters Persons by their exact age.
     *
     * @param age The age to filter by.
     * @return An unmodifiable List of matching Persons. Returns an empty list if no matches.
     */
    List<Person> filterByAge(int age);

}