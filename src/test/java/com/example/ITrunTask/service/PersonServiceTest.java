package com.example.ITrunTask.service;

import com.example.ITrunTask.model.Person;
import com.example.ITrunTask.model.Type;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class PersonServiceTest {

    private PersonService personService;

    public String internalDir = "src/main/resources/Employees/Internal";


    @Mock
    private XmlMapper xmlMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        this.personService = new PersonService(xmlMapper);
    }


    @Test
    void findAllByTypeInternalDirectoryXMLFiles() throws IOException {

        Path p1 = Paths.get(internalDir, "1.xml");
        Path p2 = Paths.get(internalDir, "2.xml");
        Path p3 = Paths.get(internalDir, "3.txt");
        Path p4 = Paths.get(internalDir, "4.xml");

        try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class);
             MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {

            pathsMockedStatic.when(() -> Paths.get(internalDir)).thenReturn(Path.of(internalDir));
            filesMockedStatic.when(() -> Files.walk(any(Path.class)))
                    .thenReturn(Stream.of(p1, p2, p3, p4));
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);

            Person person1 = new Person();
            Person person2 = new Person();
            Person person3 = new Person();

            when(xmlMapper.readValue(any(File.class), eq(Person.class)))
                    .thenReturn(person1, person2);

            List<Person> result = personService.findAllByType(Type.INTERNAL);

            assertEquals(3, result.size());
            assertTrue(result.containsAll(List.of(person1, person2, person3)));
        }
    }

    @Test
    void findByAllFieldsInInternal() throws IOException {
        Path mockInternalPath = Paths.get("internal/mock/path/to/file.xml");
        Person mockPerson = new Person("999", "Jan", "Kowalski", "123123123", "test@example.com", "97043212345");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.walk(Paths.get(internalDir)))
                    .thenReturn(Stream.of(mockInternalPath));

            mockedFiles.when(() -> Files.isRegularFile(any(Path.class)))
                    .thenReturn(true);

            when(xmlMapper.readValue(any(File.class), eq(Person.class))).thenReturn(mockPerson);

            List<Person> result = personService.findBy("Jan", "Kowalski", "test@example.com");

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals("Jan", result.get(0).getFirstName());
            assertEquals("Kowalski", result.get(0).getLastName());
            assertEquals("test@example.com", result.get(0).getEmail());
        }
    }


    @Test
    void updateExistingPersonDetails() throws IOException{
        String idToUpdate = "999";
        Type type = Type.INTERNAL;
        String directoryDest = internalDir;
        Path filePath = Paths.get(directoryDest, idToUpdate + ".xml");
        Person originalPerson = new Person(idToUpdate, "Janek", "Kowalski", "223222", "janek1@example.com", "97043212345");
        Person updatedPerson = new Person(idToUpdate, "JanNowy", "Kowalski", "123123123", "janekNowy@example.com", "97043212345");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(eq(filePath))).thenReturn(true);

            when(xmlMapper.readValue(any(File.class), eq(Person.class))).thenReturn(originalPerson);

            personService.updatePersonDetails(updatedPerson, idToUpdate, type);

            ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
            verify(xmlMapper).writeValue(eq(filePath.toFile()), personCaptor.capture());

            Person resultPerson = personCaptor.getValue();
            assertNotNull(resultPerson);
            assertEquals("janekNowy@example.com", resultPerson.getEmail());
            assertEquals("JanNowy", resultPerson.getFirstName());
            assertEquals("123123123", resultPerson.getMobile());
        }
    }

    @Test
    void deletePersonById_FileExists(){
        // Given
        String personId = "1234";
        Type type = Type.INTERNAL;
        String directoryDest = internalDir;
        Path filePath = Paths.get(directoryDest, personId + ".xml");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(eq(filePath))).thenReturn(true);
            boolean result = personService.deletePersonById(personId, type);
            assertTrue(result, "The file should be successfully deleted.");
        }
    }

    @Test
    void deletePersonById_FileDoesNotExist(){
        String personId = "unknown";
        Type type = Type.INTERNAL;
        String directoryDest = internalDir;
        Path filePath = Paths.get(directoryDest, personId + ".xml");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(eq(filePath))).thenReturn(false);
            boolean result = personService.deletePersonById(personId, type);
            assertFalse(result, "The file does not exist and hence cannot be deleted.");
        }
    }

    @Test
    void createdPersonFileSuccessfully() throws IOException  {
        Person mockPerson =
                new Person("999", "testName", "testLastName", "123123123", "testEmail@gmail.com", "95023213422");
        Type mockType = Type.INTERNAL;
        personService.createPerson(mockPerson, mockType);

        Path expectedFilePath = Paths.get(internalDir, mockPerson.getPersonId() + ".xml");
        assertTrue(Files.exists(expectedFilePath));

        Files.delete(expectedFilePath);
    }
}