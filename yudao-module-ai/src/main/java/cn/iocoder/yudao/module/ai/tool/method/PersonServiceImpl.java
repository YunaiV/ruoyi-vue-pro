package cn.iocoder.yudao.module.ai.tool.method;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 来自 Spring AI 官方文档
 *
 * Implementation of the PersonService interface using an in-memory data store.
 * Manages a collection of Person objects loaded from embedded CSV data.
 * This class is thread-safe due to the use of ConcurrentHashMap and AtomicInteger.
 */
@Service
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final Map<Integer, Person> personStore = new ConcurrentHashMap<>();

    private AtomicInteger idGenerator;

    /**
     * Embedded CSV data for initial population
     */
    private static final String CSV_DATA = """
            Id,FirstName,LastName,Email,Sex,IpAddress,JobTitle,Age
            1,Fons,Tollfree,ftollfree0@senate.gov,Male,55.1 Tollfree Lane,Research Associate,31
            2,Emlynne,Tabourier,etabourier1@networksolutions.com,Female,18 Tabourier Way,Associate Professor,38
            3,Shae,Johncey,sjohncey2@yellowpages.com,Male,1 Johncey Circle,Structural Analysis Engineer,30
            4,Sebastien,Bradly,sbradly3@mapquest.com,Male,2 Bradly Hill,Chief Executive Officer,40
            5,Harriott,Kitteringham,hkitteringham4@typepad.com,Female,3 Kitteringham Drive,VP Sales,47
            6,Anallise,Parradine,aparradine5@miibeian.gov.cn,Female,4 Parradine Street,Analog Circuit Design manager,44
            7,Gorden,Kirkbright,gkirkbright6@reuters.com,Male,5 Kirkbright Plaza,Senior Editor,40
            8,Veradis,Ledwitch,vledwitch7@google.com.au,Female,6 Ledwitch Avenue,Computer Systems Analyst IV,44
            9,Agnesse,Penhalurick,apenhalurick8@google.it,Female,7 Penhalurick Terrace,Automation Specialist IV,41
            10,Bibby,Hutable,bhutable9@craigslist.org,Female,8 Hutable Place,Account Representative I,43
            11,Karoly,Lightoller,klightollera@rakuten.co.jp,Female,9 Lightoller Parkway,Senior Developer,46
            12,Cristine,Durrad,cdurradb@aol.com,Female,10 Durrad Center,Senior Developer,48
            13,Aggy,Napier,anapierc@hostgator.com,Female,11 Napier Court,VP Product Management,44
            14,Prisca,Caddens,pcaddensd@vinaora.com,Female,12 Caddens Alley,Business Systems Development Analyst,41
            15,Khalil,McKernan,kmckernane@google.fr,Male,13 McKernan Pass,Engineer IV,44
            16,Lorry,MacTrusty,lmactrustyf@eventbrite.com,Male,14 MacTrusty Junction,Design Engineer,42
            17,Casandra,Worsell,cworsellg@goo.gl,Female,15 Worsell Point,Systems Administrator IV,45
            18,Ulrikaumeko,Haveline,uhavelineh@usgs.gov,Female,16 Haveline Trail,Financial Advisor,42
            19,Shurlocke,Albany,salbanyi@artisteer.com,Male,17 Albany Plaza,Software Test Engineer III,46
            20,Myrilla,Brimilcombe,mbrimilcombej@accuweather.com,Female,18 Brimilcombe Road,Programmer Analyst I,48
            21,Carlina,Scimonelli,cscimonellik@va.gov,Female,19 Scimonelli Pass,Help Desk Technician,45
            22,Tina,Goullee,tgoulleel@miibeian.gov.cn,Female,20 Goullee Crossing,Accountant IV,43
            23,Adriaens,Storek,astorekm@devhub.com,Female,21 Storek Avenue,Recruiting Manager,40
            24,Tedra,Giraudot,tgiraudotn@wiley.com,Female,22 Giraudot Terrace,Speech Pathologist,47
            25,Josiah,Soares,jsoareso@google.nl,Male,23 Soares Street,Tax Accountant,45
            26,Kayle,Gaukrodge,kgaukrodgep@wikispaces.com,Female,24 Gaukrodge Parkway,Accountant II,43
            27,Ardys,Chuter,achuterq@ustream.tv,Female,25 Chuter Drive,Engineer IV,41
            28,Francyne,Baudinet,fbaudinetr@newyorker.com,Female,26 Baudinet Center,VP Accounting,48
            29,Gerick,Bullan,gbullans@seesaa.net,Male,27 Bullan Way,Senior Financial Analyst,43
            30,Northrup,Grivori,ngrivorit@unc.edu,Male,28 Grivori Plaza,Systems Administrator I,45
            31,Town,Duguid,tduguidu@squarespace.com,Male,29 Duguid Pass,Safety Technician IV,46
            32,Pierette,Kopisch,pkopischv@google.com.br,Female,30 Kopisch Lane,Director of Sales,41
            33,Jacquenetta,Le Prevost,jleprevostw@netlog.com,Female,31 Le Prevost Trail,Senior Developer,47
            34,Garvy,Rusted,grustedx@aboutads.info,Male,32 Rusted Junction,Senior Developer,42
            35,Clarice,Aysh,cayshy@merriam-webster.com,Female,33 Aysh Avenue,VP Quality Control,40
            36,Tracie,Fedorski,tfedorskiz@bloglines.com,Male,34 Fedorski Terrace,Design Engineer,44
            37,Noelyn,Matushenko,nmatushenko10@globo.com,Female,35 Matushenko Place,VP Sales,48
            38,Rudiger,Klaesson,rklaesson11@usnews.com,Male,36 Klaesson Road,Database Administrator IV,43
            39,Mirella,Syddie,msyddie12@geocities.jp,Female,37 Syddie Circle,Geological Engineer,46
            40,Donalt,O'Lunny,dolunny13@elpais.com,Male,38 O'Lunny Center,Analog Circuit Design manager,41
            41,Guntar,Deniskevich,gdeniskevich14@google.com.hk,Male,39 Deniskevich Way,Structural Engineer,47
            42,Hort,Shufflebotham,hshufflebotham15@about.me,Male,40 Shufflebotham Court,Structural Analysis Engineer,45
            43,Dominique,Thickett,dthickett16@slashdot.org,Male,41 Thickett Crossing,Safety Technician I,42
            44,Zebulen,Piscopello,zpiscopello17@umich.edu,Male,42 Piscopello Parkway,Web Developer II,40
            45,Mellicent,Mac Giany,mmacgiany18@state.tx.us,Female,43 Mac Giany Pass,Assistant Manager,44
            46,Merle,Bounds,mbounds19@amazon.co.jp,Female,44 Bounds Alley,Systems Administrator III,41
            47,Madelle,Farbrace,mfarbrace1a@xinhuanet.com,Female,45 Farbrace Terrace,Quality Engineer,48
            48,Galvin,O'Sheeryne,gosheeryne1b@addtoany.com,Male,46 O'Sheeryne Way,Environmental Specialist,43
            49,Guillemette,Bootherstone,gbootherstone1c@nationalgeographic.com,Female,47 Bootherstone Plaza,Professor,46
            50,Letti,Aylmore,laylmore1d@vinaora.com,Female,48 Aylmore Circle,Automation Specialist I,40
            51,Nonie,Rivalland,nrivalland1e@weather.com,Female,49 Rivalland Avenue,Software Test Engineer IV,45
            52,Jacquelynn,Halfacre,jhalfacre1f@surveymonkey.com,Female,50 Halfacre Pass,Geologist II,42
            53,Anderea,MacKibbon,amackibbon1g@weibo.com,Female,51 MacKibbon Parkway,Automation Specialist II,47
            54,Wash,Klimko,wklimko1h@slashdot.org,Male,52 Klimko Alley,Database Administrator I,40
            55,Flori,Kynett,fkynett1i@auda.org.au,Female,53 Kynett Trail,Quality Control Specialist,46
            56,Libbey,Penswick,lpenswick1j@google.co.uk,Female,54 Penswick Point,VP Accounting,43
            57,Silvanus,Skellorne,sskellorne1k@booking.com,Male,55 Skellorne Drive,Account Executive,48
            58,Carmine,Mateos,cmateos1l@plala.or.jp,Male,56 Mateos Terrace,Systems Administrator I,41
            59,Sheffie,Blazewicz,sblazewicz1m@google.com.au,Male,57 Blazewicz Center,VP Sales,44
            60,Leanor,Worsnop,lworsnop1n@uol.com.br,Female,58 Worsnop Plaza,Systems Administrator III,45
            61,Caspar,Pamment,cpamment1o@google.co.jp,Male,59 Pamment Court,Senior Financial Analyst,42
            62,Justinian,Pentycost,jpentycost1p@sciencedaily.com,Male,60 Pentycost Way,Senior Quality Engineer,47
            63,Gerianne,Jarnell,gjarnell1q@bing.com,Female,61 Jarnell Avenue,Help Desk Operator,40
            64,Boycie,Zanetto,bzanetto1r@about.com,Male,62 Zanetto Place,Quality Engineer,46
            65,Camilla,Mac Giany,cmacgiany1s@state.gov,Female,63 Mac Giany Parkway,Senior Cost Accountant,43
            66,Hadlee,Piscopiello,hpiscopiello1t@artisteer.com,Male,64 Piscopiello Street,Account Representative III,48
            67,Bobbie,Penvarden,bpenvarden1u@google.cn,Male,65 Penvarden Lane,Help Desk Operator,41
            68,Ali,Gowlett,agowlett1v@parallels.com,Male,66 Gowlett Pass,VP Marketing,44
            69,Olivette,Acome,oacome1w@qq.com,Female,67 Acome Hill,VP Product Management,45
            70,Jehanna,Brotherheed,jbrotherheed1x@google.nl,Female,68 Brotherheed Junction,Database Administrator III,42
            71,Morgan,Berthomieu,mberthomieu1y@artisteer.com,Male,69 Berthomieu Alley,Systems Administrator II,47
            72,Linzy,Shilladay,lshilladay1z@icq.com,Female,70 Shilladay Trail,Research Assistant IV,40
            73,Faydra,Brimner,fbrimner20@mozilla.org,Female,71 Brimner Road,Senior Editor,46
            74,Gwenore,Oxlee,goxlee21@devhub.com,Female,72 Oxlee Terrace,Systems Administrator II,43
            75,Evangelin,Beinke,ebeinke22@mozilla.com,Female,73 Beinke Circle,Accountant I,48
            76,Missy,Cockling,mcockling23@si.edu,Female,74 Cockling Way,Software Engineer I,41
            77,Suzanne,Klimschak,sklimschak24@etsy.com,Female,75 Klimschak Plaza,Tax Accountant,44
            78,Candide,Goricke,cgoricke25@weebly.com,Female,76 Goricke Pass,Sales Associate,45
            79,Gerome,Pinsent,gpinsent26@google.com.au,Male,77 Pinsent Junction,Software Consultant,42
            80,Lezley,Mac Giany,lmacgiany27@scribd.com,Male,78 Mac Giany Alley,Operator,47
            81,Tobiah,Durn,tdurn28@state.tx.us,Male,79 Durn Court,VP Sales,40
            82,Sherlocke,Cockshoot,scockshoot29@yelp.com,Male,80 Cockshoot Street,Senior Financial Analyst,46
            83,Myrle,Speenden,mspeenden2a@utexas.edu,Female,81 Speenden Center,Senior Developer,43
            84,Isidore,Gorries,igorries2b@flavors.me,Male,82 Gorries Parkway,Sales Representative,48
            85,Isac,Kitchingman,ikitchingman2c@businessinsider.com,Male,83 Kitchingman Drive,VP Accounting,41
            86,Benedetta,Purrier,bpurrier2d@admin.ch,Female,84 Purrier Trail,VP Accounting,44
            87,Tera,Fitchell,tfitchell2e@fotki.com,Female,85 Fitchell Place,Software Engineer IV,45
            88,Abbe,Pamment,apamment2f@about.com,Male,86 Pamment Avenue,VP Sales,42
            89,Jandy,Gommowe,jgommowe2g@angelfire.com,Female,87 Gommowe Road,Financial Analyst,47
            90,Karena,Fussey,kfussey2h@google.com.au,Female,88 Fussey Point,Assistant Professor,40
            91,Gaspar,Pammenter,gpammenter2i@google.com.br,Male,89 Pammenter Hill,Help Desk Operator,46
            92,Stanwood,Mac Giany,smacgiany2j@prlog.org,Male,90 Mac Giany Terrace,Research Associate,43
            93,Byrom,Beedell,bbeedell2k@google.co.jp,Male,91 Beedell Way,VP Sales,48
            94,Annabella,Rowbottom,arowbottom2l@google.com.au,Female,92 Rowbottom Plaza,Help Desk Operator,41
            95,Rodolphe,Debell,rdebell2m@imageshack.us,Male,93 Debell Pass,Design Engineer,44
            96,Tyne,Gommey,tgommey2n@joomla.org,Female,94 Gommey Junction,VP Marketing,45
            97,Christoper,Pincked,cpincked2o@icq.com,Male,95 Pincked Alley,Human Resources Manager,42
            98,Kore,Le Prevost,kleprevost2p@tripadvisor.com,Female,96 Le Prevost Street,VP Quality Control,47
            99,Ceciley,Petrolli,cpetrolli2q@oaic.gov.au,Female,97 Petrolli Court,Senior Developer,40
            100,Elspeth,Mac Giany,emacgiany2r@icio.us,Female,98 Mac Giany Parkway,Internal Auditor,46
            """;

    /**
     * Initializes the service after dependency injection by loading data from the CSV string.
     * Uses @PostConstruct to ensure this runs after the bean is created.
     */
    @PostConstruct
    private void initializeData() {
        log.info("Initializing PersonService data store...");
        int maxId = loadDataFromCsv();
        idGenerator = new AtomicInteger(maxId);
        log.info("PersonService initialized with {} records. Next ID: {}", personStore.size(), idGenerator.get() + 1);
    }

    /**
     * Parses the embedded CSV data and populates the in-memory store.
     * Calculates the maximum ID found in the data to initialize the ID generator.
     *
     * @return The maximum ID found in the loaded CSV data.
     */
    private int loadDataFromCsv() {
        final AtomicInteger currentMaxId = new AtomicInteger(0);
        // Clear existing data before loading (important for tests or re-initialization scenarios)
        personStore.clear();
        try (Stream<String> lines = CSV_DATA.lines().skip(1)) { // Skip header row
            lines.forEach(line -> {
                try {
                    // Split carefully, handling potential commas within quoted fields if necessary (simple split here)
                    String[] fields = line.split(",", 8); // Limit split to handle potential commas in job title
                    if (fields.length == 8) {
                        int id = Integer.parseInt(fields[0].trim());
                        String firstName = fields[1].trim();
                        String lastName = fields[2].trim();
                        String email = fields[3].trim();
                        String sex = fields[4].trim();
                        String ipAddress = fields[5].trim();
                        String jobTitle = fields[6].trim();
                        int age = Integer.parseInt(fields[7].trim());

                        Person person = new Person(id, firstName, lastName, email, sex, ipAddress, jobTitle, age);
                        personStore.put(id, person);
                        currentMaxId.updateAndGet(max -> Math.max(max, id));
                    } else {
                        log.warn("Skipping malformed CSV line (expected 8 fields, found {}): {}", fields.length, line);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Skipping line due to parsing error (ID or Age): {} - Error: {}", line, e.getMessage());
                } catch (Exception e) {
                    log.error("Skipping line due to unexpected error: {} - Error: {}", line, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Fatal error reading embedded CSV data: {}", e.getMessage(), e);
            // In a real application, might throw a specific initialization exception
        }
        return currentMaxId.get();
    }

    @Override
    @Tool(
        name = "ps_create_person",
        description = "Create a new person record in the in-memory store."
    )
    public Person createPerson(Person personData) {
        if (personData == null) {
            throw new IllegalArgumentException("Person data cannot be null");
        }
        int newId = idGenerator.incrementAndGet();
        // Create a new Person record using data from the input, but with the generated ID
        Person newPerson = new Person(
                newId,
                personData.firstName(),
                personData.lastName(),
                personData.email(),
                personData.sex(),
                personData.ipAddress(),
                personData.jobTitle(),
                personData.age()
        );
        personStore.put(newId, newPerson);
        log.debug("Created person: {}", newPerson);
        return newPerson;
    }

    @Override
    @Tool(
        name = "ps_get_person_by_id",
        description = "Retrieve a person record by ID from the in-memory store."
    )
    public Optional<Person> getPersonById(int id) {
        Person person = personStore.get(id);
        log.debug("Retrieved person by ID {}: {}", id, person);
        return Optional.ofNullable(person);
    }

    @Override
    @Tool(
        name = "ps_get_all_persons",
        description = "Retrieve all person records from the in-memory store."
    )
    public List<Person> getAllPersons() {
        // Return an unmodifiable view of the values
        List<Person> allPersons = personStore.values().stream().toList();
        log.debug("Retrieved all persons (count: {})", allPersons.size());
        return allPersons;
    }

    @Override
    @Tool(
        name = "ps_update_person",
        description = "Update an existing person record by ID in the in-memory store."
    )
    public boolean updatePerson(int id, Person updatedPersonData) {
         if (updatedPersonData == null) {
            throw new IllegalArgumentException("Updated person data cannot be null");
        }
        // Use computeIfPresent for atomic update if the key exists
        Person result = personStore.computeIfPresent(id, (key, existingPerson) ->
                // Create a new Person record with the original ID but updated data
                new Person(
                        id, // Keep original ID
                        updatedPersonData.firstName(),
                        updatedPersonData.lastName(),
                        updatedPersonData.email(),
                        updatedPersonData.sex(),
                        updatedPersonData.ipAddress(),
                        updatedPersonData.jobTitle(),
                        updatedPersonData.age()
                )
        );
        boolean updated = result != null;
        log.debug("Update attempt for ID {}: {}", id, updated ? "Successful" : "Failed (Not Found)");
        if(updated) log.trace("Updated person data for ID {}: {}", id, result);
        return updated;
    }

    @Override
    @Tool(
        name = "ps_delete_person",
        description = "Delete a person record by ID from the in-memory store."
    )
    public boolean deletePerson(int id) {
        boolean removed = personStore.remove(id) != null;
        log.debug("Delete attempt for ID {}: {}", id, removed ? "Successful" : "Failed (Not Found)");
        return removed;
    }

    @Override
    @Tool(
        name = "ps_search_by_job_title",
        description = "Search for persons by job title in the in-memory store."
    )
    public List<Person> searchByJobTitle(String jobTitleQuery) {
        if (jobTitleQuery == null || jobTitleQuery.isBlank()) {
            log.debug("Search by job title skipped due to blank query.");
            return Collections.emptyList();
        }
        String lowerCaseQuery = jobTitleQuery.toLowerCase();
        List<Person> results = personStore.values().stream()
                .filter(person -> person.jobTitle() != null && person.jobTitle().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
        log.debug("Search by job title '{}' found {} results.", jobTitleQuery, results.size());
        return Collections.unmodifiableList(results);
    }

    @Override
    @Tool(
        name = "ps_filter_by_sex",
        description = "Filters Persons by sex (case-insensitive)."
    )
    public List<Person> filterBySex(String sex) {
        if (sex == null || sex.isBlank()) {
             log.debug("Filter by sex skipped due to blank filter.");
            return Collections.emptyList();
        }
        List<Person> results = personStore.values().stream()
                .filter(person -> person.sex() != null && person.sex().equalsIgnoreCase(sex))
                .collect(Collectors.toList());
        log.debug("Filter by sex '{}' found {} results.", sex, results.size());
        return Collections.unmodifiableList(results);
    }

    @Override
    @Tool(
        name = "ps_filter_by_age",
        description = "Filters Persons by age."
    )
    public List<Person> filterByAge(int age) {
         if (age < 0) {
            log.debug("Filter by age skipped due to negative age: {}", age);
            return Collections.emptyList(); // Or throw IllegalArgumentException based on requirements
        }
        List<Person> results = personStore.values().stream()
                .filter(person -> person.age() == age)
                .collect(Collectors.toList());
        log.debug("Filter by age {} found {} results.", age, results.size());
        return Collections.unmodifiableList(results);
    }

}