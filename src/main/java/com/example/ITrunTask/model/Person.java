package com.example.ITrunTask.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "Person")
public class Person {

    @JacksonXmlProperty
    private String personId;
    @JacksonXmlProperty
    private String firstName;
    @JacksonXmlProperty
    private String lastName;
    @JacksonXmlProperty
    private String mobile;
    @JacksonXmlProperty
    private String email;
    @JacksonXmlProperty
    private String pesel;
}
