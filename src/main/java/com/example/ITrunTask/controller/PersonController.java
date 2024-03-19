package com.example.ITrunTask.controller;

import com.example.ITrunTask.model.Person;
import com.example.ITrunTask.model.Type;
import com.example.ITrunTask.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/person")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService service) {
        this.personService = service;
    }

    @GetMapping(value = "/find-all-by-type")
    public ResponseEntity<List<Person>> findAllByType(@RequestParam Type type) {
        try{
            return new ResponseEntity<>(personService.findAllByType(type), HttpStatus.OK);
        }catch (IOException e){
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/find-by")
    public ResponseEntity<List<Person>> findBy(@RequestParam(required = false) String firstName,
                                               @RequestParam(required = false) String lastName,
                                               @RequestParam(required = false) String email) {
        try{
            return new ResponseEntity<>(personService.findBy(firstName, lastName, email), HttpStatus.OK);
        }catch (IOException e){
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(value = "/save", consumes = "application/xml")
    public ResponseEntity<String> save(@RequestBody Person person,
                     @RequestParam Type type){
        if(personService.fileExist(type, person.getPersonId())){
            return new ResponseEntity<>("Person with id: " + person.getPersonId() + " exists in directory: " + type, HttpStatus.BAD_REQUEST);
        }
        personService.createPerson(person, type);
        return new ResponseEntity<>("Person saved..", HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{personId}")
    public ResponseEntity<String> deleteById(@PathVariable String personId,
                           @RequestParam Type type){
        if(personService.deletePersonById(personId, type)){
            return new ResponseEntity<>("Successfully deleted..", HttpStatus.OK);
        }

        return new ResponseEntity<>("Did not found person to delete..", HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/update", consumes = "application/xml")
    public ResponseEntity<String> update(@RequestParam Type type,
                                         @RequestBody Person updatedPerson){
        if(updatedPerson.getPersonId() == null || updatedPerson.getPersonId().isEmpty() || !personService.fileExist(type, updatedPerson.getPersonId())){
            return ResponseEntity.badRequest().body("Bad ID to update.");
        }
        personService.updatePersonDetails(updatedPerson, updatedPerson.getPersonId(), type);
        return ResponseEntity.ok().body("Data successfully updated.");
    }
}
