package com.ecommerce.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a single user object returned by reqres.in. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private int id;
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String avatar;

    public int getId()           { return id; }
    public String getEmail()     { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getAvatar()    { return avatar; }

    public void setId(int id)                  { this.id = id; }
    public void setEmail(String email)         { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setAvatar(String avatar)       { this.avatar = avatar; }
}
