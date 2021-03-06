package no.java.ems.server;

import no.java.ems.dao.EventDao;
import no.java.ems.dao.PersonDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.domain.EmailAddress;
import no.java.ems.domain.Event;
import no.java.ems.domain.Language;
import no.java.ems.domain.Person;
import no.java.ems.domain.Room;
import no.java.ems.domain.Session;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.drda.NetworkServerControl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.net.InetAddress;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DerbyService implements Stoppable {

    private Log log = LogFactory.getLog(getClass());

    private String[] TABLES = new String[]{
        "person",
        "room",
        "event",
        "session",
        "session_speaker",
        "person_attachement",
        "event_attachement",
        "event_room",
        "event_timeslot",
        "session_attachement",
    };

    private JdbcTemplate jdbcTemplate;
    private EventDao eventDao;
    private PersonDao personDao;
    private SessionDao sessionDao;
    private RoomDao roomDao;
    private File dbHome;
    private int port;

    private NetworkServerControl derbyServer;

    public DerbyService(JdbcTemplate jdbcTemplate, EventDao eventDao, PersonDao personDao, SessionDao sessionDao,
                        RoomDao roomDao, File dbHome, int port) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventDao = eventDao;
        this.personDao = personDao;
        this.sessionDao = sessionDao;
        this.roomDao = roomDao;
        this.dbHome = dbHome;
        this.port = port;
    }

    public void startNetworkServer() throws Exception {
        InetAddress localhost = InetAddress.getByName("0.0.0.0");

        System.setProperty("derby.system.home", dbHome.getAbsolutePath());
        System.setProperty("derby.system.bootAll", "true");

        derbyServer = new NetworkServerControl(localhost, port, "sa", "sa");
        derbyServer.start(new PrintWriter(System.out));

        log.info("Startet network server on " + localhost.toString() + ":" + port);
        log.info("Db home: " + dbHome.getAbsolutePath());
    }

    public void stop() throws Exception {
        derbyServer.shutdown();
    }

    public boolean maybeCreateTables(boolean dropTables) {
        boolean createTables = false;
        try {
            jdbcTemplate.queryForInt("select count(*) from person");
        } catch (DataAccessException e) {
            createTables = true;
        }
        if (dropTables) {
            System.err.println("--- dropping tables:");
            List<String> tables = new ArrayList<String>(Arrays.asList(TABLES));
            Collections.reverse(tables);
            for (String table : tables) {
                System.err.print("Dropping table " + table);
                jdbcTemplate.update("drop table " + table);
                System.err.println("ok!");
            }
            createTables = true;
        }
        if (createTables) {
            System.err.println("--- creating " + TABLES.length + " tables:");

            for (String table : TABLES) {
                System.err.print("--- creating table " + table + "...");
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ddl/" + table + ".ddl");
                if(inputStream == null){
                    System.err.println("Could not find DDL file for table " + table + "!");
                    return false;
                }

                try {
                    String sql = IOUtils.toString(inputStream);
                    jdbcTemplate.execute(sql);
                } catch (IOException e) {
                    System.err.println("Failed!");
                    e.printStackTrace();
                    return false;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }

                System.err.println("ok");
            }

            Event event = new Event();
            event.setName("JavaZone 2008");
            ArrayList<Room> rooms = new ArrayList<Room>();
            rooms.add(saveRoom("Gate 1"));
            rooms.add(saveRoom("Gate 2"));
            rooms.add(saveRoom("Gate 3"));
            rooms.add(saveRoom("Gate 4"));
            rooms.add(saveRoom("Gate 5"));
            rooms.add(saveRoom("Gate 6"));
            rooms.add(saveRoom("Gate 7"));
            rooms.add(saveRoom("BoF"));
            rooms.add(saveRoom("Food"));
            event.setRooms(rooms);
            eventDao.saveEvent(event);

            final int count = 0;
            for (int n = 0; n < count; n++) {
                Person person = new Person(String.format("Person %02d", n));
                if (n % 2 == 0) {
                    person.setLanguage(new Language("no"));
                } else {
                    person.setLanguage(new Language("en"));
                }
                if (n % 25 == 0) {
                    person.setGender(Person.Gender.Female);
                    person.setEmailAddresses(Arrays.asList(new EmailAddress("jane@doe.com")));
                } else {
                    person.setGender(Person.Gender.Male);
                    person.setEmailAddresses(Arrays.asList(new EmailAddress("john@doe.com")));
                }
                person.setDescription("Public description...");
                person.setNotes("Internal notes...");
                person.setTags(new ArrayList<String>(Arrays.asList("tag1,tag2,tag3".split(","))));
                personDao.savePerson(person);
            }
            if (count > 0) {
                System.err.println(String.format("added %d people to database", count));
            }

            for (int n = 0; n < count; n++) {
                Session session = new Session(String.format("Session %02d", n));
                if (n % 15 == 0) {
                    session.setState(Session.State.Rejected);
                } else if (n % 5 == 0) {
                    session.setState(Session.State.Approved);
                } else {
                    session.setState(Session.State.Pending);
                }
                session.setEventId(event.getId());
                sessionDao.saveSession(session);
            }
            if (count > 0) {
                System.err.println(String.format("added %d sessions to database", count));
            }
        }

        return true;
    }

    private Room saveRoom(String name) {
        Room room = new Room(name);
        roomDao.save(room);
        return room;
    }
}
