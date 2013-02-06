package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.dao.PersonDao;
import no.java.ems.domain.Binary;
import no.java.ems.domain.EmailAddress;
import no.java.ems.domain.Language;
import no.java.ems.domain.Address;
import no.java.ems.domain.Person;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author <a href="mailto:partha.guha.roy@gmail.com">Partha Roy</a>
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class JdbcTemplatePersonDao implements PersonDao {

    private static final String DELIMITER = ",";
    private final JdbcTemplate jdbcTemplate;
    private BinaryDao binaryDao;

    public JdbcTemplatePersonDao(final JdbcTemplate jdbcTemplate, BinaryDao binaryDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.binaryDao = binaryDao;
    }

    public Person getPerson(final String id) {
        String queryForSingleSql = "select * from person where id = ?";
        return (Person)jdbcTemplate.queryForObject(
                queryForSingleSql, new Object[]{id},
                new int[]{Types.VARCHAR}, new PersonMapper()
        );
    }

    public List<Person> getPersons() {
        // noinspection unchecked
        return jdbcTemplate.query("select * from person order by name", new PersonMapper());
    }

    public void savePerson(final Person person) {
        String updateSql;
        Binary photoBinary = person.getPhoto();
        String photoUri = null;
        if (photoBinary != null) {
            photoUri = photoBinary.getId();
        }

        if (person.getId() == null) {
            updateSql = "insert into person (revision, tags, name, description, gender, birthdate, language, countryCode, zipCode, addresses, notes, photo, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            person.setId(UUID.randomUUID().toString());
            person.setRevision(1);
        } else {
            updateSql = "update person set revision = ?, tags = ?, name = ?, description = ?, gender = ?, birthdate = ?, language = ?, countryCode = ?, zipCode = ?, addresses = ?, notes = ?, photo=? where id = ?";
            person.setRevision(person.getRevision() + 1);
        }
        jdbcTemplate.update(
            updateSql,
            new Object[]{
                person.getRevision(),
                person.getTagsAsString(DELIMITER),
                person.getName(),
                person.getDescription(),
                person.getGender().name(),
                person.getBirthdate() == null ? null : new Date(
                    person.getBirthdate().toDateTimeAtStartOfDay().toDate()
                        .getTime()
                ), 
                person.getLanguage() == null ? null : person.getLanguage().getIsoCode(),
                person.getAddress() == null ? null : person.getAddress().getIsoCode(),
                person.getAddress() == null ? null : person.getAddress().getZipCode(),
                person.getEmailAddressesAsString(DELIMITER), person.getNotes(), photoUri, person.getId()},
            new int[]{
                Types.INTEGER, // revision
                Types.LONGVARCHAR, // tags
                Types.VARCHAR, // name
                Types.LONGVARCHAR, // description
                Types.VARCHAR, // gender
                Types.DATE, // birthdate
                Types.VARCHAR, // language
                Types.VARCHAR, // countryCode
                Types.VARCHAR, // zipCode
                Types.LONGVARCHAR, // addresses
                Types.LONGVARCHAR, // notes
                Types.VARCHAR, // photoUri
                Types.VARCHAR, // id
            }
        );

        jdbcTemplate.update("delete from person_attachement where personId = ?", new Object[]{person.getId()});
        List<Binary> attachements = person.getAttachements();
        for (int position = 0; position < attachements.size(); position++) {
            Binary attachement = attachements.get(position);
            jdbcTemplate.update(
                    "insert into person_attachement values (?, ?, ?)",
                    new Object[]{
                            person.getId(),
                            attachement.getId(),
                            position,
                    },
                    new int[]{
                            Types.VARCHAR,     // personId
                            Types.VARCHAR,     // attachementId
                            Types.INTEGER,     // position
                    }

            );
        }
    }

    public void deletePerson(final String id) {
        jdbcTemplate.update("delete from person_attachement where personId = ?", new Object[]{id});
        jdbcTemplate.update("delete from person where id = ?", new Object[]{id}, new int[]{Types.VARCHAR});
    }

    private static LocalDate toLocalDate(final Date date) {
        return date == null ? null : new LocalDate(date.getTime());
    }

    private class PersonMapper implements RowMapper {

        public Person mapRow(final ResultSet resultSet, final int rowNumber) throws SQLException {
            Person person = new Person();
            person.setId(resultSet.getString("id"));
            person.setRevision(resultSet.getInt("revision"));
            String tags = resultSet.getString("tags");
            if (tags != null) {
                person.setTags(Arrays.asList(tags.split(DELIMITER)));
            }
            person.setName(resultSet.getString("name"));
            person.setNotes(resultSet.getString("notes"));
            person.setDescription(resultSet.getString("description"));
            person.setGender(Person.Gender.valueOf(resultSet.getString("gender")));
            person.setBirthdate(toLocalDate(resultSet.getDate("birthdate")));
            person.setLanguage(Language.valueOf(resultSet.getString("language")));
            person.setAddress(Address.valueOf(resultSet.getString("countryCode"), resultSet.getString("zipCode")));
            String addresses = resultSet.getString("addresses");
            List<EmailAddress> emailAddresses = new ArrayList<EmailAddress>();
            if (addresses != null) {
                for (String address : addresses.split(DELIMITER)) {
                    if (address.length() != 0) {
                        emailAddresses.add(new EmailAddress(address));
                    }
                }
            }
            person.setEmailAddresses(emailAddresses);
            String photoId = resultSet.getString("photo");
            if (photoId != null) {
                person.setPhoto(binaryDao.getBinary(photoId));
            }
            //noinspection unchecked
            person.setAttachements(
                    jdbcTemplate.query(
                            "select attachementId from person_attachement where personId = ? order by position",
                            new Object[]{person.getId()},
                            new RowMapper() {
                                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return binaryDao.getBinary(rs.getString("attachementId"));
                                }
                            }
                    )

            );
            return person;
        }

    }

}
