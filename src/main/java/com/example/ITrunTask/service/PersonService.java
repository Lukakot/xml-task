package com.example.ITrunTask.service;

import com.example.ITrunTask.model.Person;
import com.example.ITrunTask.model.Type;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class PersonService {

    private final String internalDirectory = "src/main/resources/Employees/Internal";

    private final String externalDirectory = "src/main/resources/Employees/External";

    private static final String XML_EXTENSION = ".xml";

    private final XmlMapper xmlMapper;

    public PersonService(XmlMapper xmlMapper){
        this.xmlMapper = xmlMapper;
    }

    public List<Person> findAllByType(Type type) throws IOException {
        String directory = determineIfInternalOrExternal(type);
        List<Person> foundPersonList = new ArrayList<>();
        Path completePath = Paths.get(directory);
        List<File> xmlFiles;
        try(Stream<Path> pathStream = Files.walk(completePath)){
             xmlFiles = pathStream
                    .filter(Files::isRegularFile)  // wykluczenie pliku nadrzędnego
                    .map(Path::toFile)
                    .filter(file -> file.toString().endsWith(".xml"))
                    .toList();
        }

        xmlFiles.forEach(file -> {
            try {
                Person p = xmlMapper.readValue(file, Person.class);
                foundPersonList.add(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return foundPersonList;
    }


    public void createPerson(Person person, Type type){
        try {
            String saveDestinationFolder = determineIfInternalOrExternal(type);
            Path path = Paths.get(saveDestinationFolder, person.getPersonId() + XML_EXTENSION);
            File file = path.toFile();
            try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
                xmlMapper.writeValue(fileOutputStream, person);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deletePersonById(String personId, Type type){
        String directoryDest = determineIfInternalOrExternal(type);
        Path filePath = Paths.get(directoryDest, personId + XML_EXTENSION);
        try {
            return Files.deleteIfExists(filePath);
        }catch (Exception e){
            throw new RuntimeException("Error during deleting person with ID: " + personId, e);
        }
    }



    public void updatePersonDetails(Person updatedPerson, String idToUpdate, Type type){
        String directoryDest = determineIfInternalOrExternal(type);
        Path filePath = Paths.get(directoryDest, idToUpdate + XML_EXTENSION);

        try{
            if(Files.exists(filePath)){
                Person person = xmlMapper.readValue(filePath.toFile(), Person.class);
                log.info("Found person to update -> {}", person.toString());

                Map<String, BiConsumer<Person, Person>> updateMap = fieldsToUpdateMap();
                updateMap.forEach((field, biFunction) -> biFunction.accept(person, updatedPerson));

                xmlMapper.writeValue(filePath.toFile(), person);
            }else{
                log.info("No person to update..");
            }
        } catch (IOException e) {
            throw new RuntimeException("exception occurred: " + e);
        }
    }

    private Map<String, BiConsumer<Person, Person>> fieldsToUpdateMap(){
        Map<String, BiConsumer<Person, Person>> biConsumerMap = new HashMap<>();
        biConsumerMap.put("firstName", (existing, updated) -> {
            if (updated.getFirstName() != null){
                existing.setFirstName(updated.getFirstName());
            }
        });
        biConsumerMap.put("lastName", (existing, updated) -> {
            if (updated.getLastName() != null){
                existing.setLastName(updated.getLastName());
            }
        });
        biConsumerMap.put("mobile", (existing, updated) -> {
            if (updated.getMobile() != null){
                existing.setMobile(updated.getMobile());
            }
        });
        biConsumerMap.put("email", (existing, updated) -> {
            if (updated.getEmail() != null){
                existing.setEmail(updated.getEmail());
            }
        });
        biConsumerMap.put("pesel", (existing, updated) -> {
            if (updated.getPesel() != null){
                existing.setPesel(updated.getPesel());
            }
        });

        return biConsumerMap;
    }

    public boolean fileExist(Type type, String personId){
        String directory = determineIfInternalOrExternal(type);
        Path filePath = Paths.get(directory, personId + XML_EXTENSION);
        return filePath.toFile().exists();
    }
    private String determineIfInternalOrExternal(Type type){
        return type.toString().equalsIgnoreCase("internal") ? internalDirectory : externalDirectory;
    }

    public List<Person> findBy(String firstName, String lastName, String email) throws IOException {
        List<Person> resultEntries = new ArrayList<>();
        List<String> directories = List.of(internalDirectory, externalDirectory);
        for(String dir : directories){
            try(Stream<Path> pathStream = Files.walk(Paths.get(dir))){
                    List<Person> foundEntries = pathStream
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .filter(file -> file.toString().endsWith(".xml"))
                        .map(file -> {
                            try {
                               return xmlMapper.readValue(file, Person.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(person -> matchEntry(person, firstName, lastName, email))
                        .toList();

                    resultEntries.addAll(foundEntries);
            }
        }
        return resultEntries;
    }

    private boolean matchEntry(Person person, String firstName, String lastName, String email) {
        boolean matchFirstName = firstName == null || firstName.isEmpty() || person.getFirstName().equals(firstName);
        boolean matchLastName = lastName == null || lastName.isEmpty() || person.getLastName().equals(lastName);
        boolean matchEmail = email == null || email.isEmpty() || person.getEmail().equals(email);

        return matchFirstName && matchLastName && matchEmail;
    }
}
