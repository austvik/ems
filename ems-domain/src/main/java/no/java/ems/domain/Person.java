package no.java.ems.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Person extends AbstractEntity implements Comparable<Person> {

    public enum Gender {

        Male,
        Female,

    }

    private String name;
    private String description;
    private Gender gender = Gender.Male;
    private LocalDate birthdate;
    private Language language;
    private Address address;
    private String zipCode;
    private List<EmailAddress> emailAddresses = new ArrayList<EmailAddress>();
    private Binary photo;

    public Person() {
    }

    public Person(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        firePropertyChange("name", this.name, this.name = name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        firePropertyChange("description", this.description, this.description = description);
    }

    public Gender getGender() {
        return gender;
    }

    /**
     * @throws IllegalArgumentException if gender is {@code null}.
     */
    public void setGender(final Gender gender) {
        Validate.notNull(gender, "Gender may not be null");
        firePropertyChange("gender", this.gender, this.gender = gender);
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(final LocalDate birthdate) {
        firePropertyChange("birthdate", this.birthdate, this.birthdate = birthdate);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(final Language language) {
        firePropertyChange("language", this.language, this.language = language);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        firePropertyChange("address", this.address, this.address = address);
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        firePropertyChange("zipcode", this.zipCode, this.zipCode = zipCode);
    }

    public List<EmailAddress> getEmailAddresses() {
        return Collections.unmodifiableList(emailAddresses);
    }

    public void setEmailAddresses(final List<EmailAddress> emailAddresses) {
        firePropertyChange("emailAddresses", getEmailAddresses(), Collections.unmodifiableList(this.emailAddresses = new ArrayList<EmailAddress>(emailAddresses)));
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(final Binary photo) {
        firePropertyChange("photo", this.photo, this.photo = photo);
    }

    public String getEmailAddressesAsString(final String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (EmailAddress emailAddress : emailAddresses) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(emailAddress.getEmailAddress());
        }
        return builder.toString();
    }

    /**
     * Compares based on the persons name.
     */
    public int compareTo(final Person other) {
        return new CompareToBuilder().append(name, other == null ? null : other.getName()).toComparison();
    }

    public void sync(final Person other) {
        super.sync(other);
        setName(other.getName());
        setDescription(other.getDescription());
        setGender(other.getGender());
        setBirthdate(other.getBirthdate());
        setLanguage(other.getLanguage());
        setAddress(other.getAddress());
        setEmailAddresses(other.getEmailAddresses());
        setPhoto(other.getPhoto());
    }

}
