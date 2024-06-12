package com.esi.mscours;

import com.esi.mscours.entities.*;
import com.esi.mscours.entities.Module;
import com.esi.mscours.proxy.StatisticsProxy;
import com.esi.mscours.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class MsCoursApplication implements CommandLineRunner {
    @Resource
    GroupeRepository groupeRepository;
    @Resource
    SpecialityRepository specialityRepository;
    @Resource
    ModuleRepository moduleRepository;
    @Resource
    LectureRepository lectureRepository;
    @Resource
    DocumentRepository documentRepository;
    @Resource
    ConferenceRepository conferenceRepository;

    public static void main(String[] args) {
        SpringApplication.run(MsCoursApplication.class, args);
    }

    @Autowired
    StatisticsProxy statisticsProxy;
    @Override
    public void run(String... args) throws Exception {

        Document document1 = new Document();
        document1.setLink("http://example.com/doc1");
        document1.setName("doc 1");
        document1.setIdTeacher(2L);
        document1 = documentRepository.save(document1);

        Document document2 = new Document();
        document2.setLink("http://example.com/doc2");
        document2.setName("doc 2");
        document2.setIdTeacher(2L);
        document2 = documentRepository.save(document2);

        // Limit to first 2 specialities
        List<SpecialityName> limitedSpecialities = Arrays.asList(SpecialityName.values()).subList(0, 2);
        for (SpecialityName specialityName : limitedSpecialities) {
            Speciality speciality = new Speciality(null, specialityName, null);
            speciality = specialityRepository.save(speciality);

            // Limit to first 2 modules per speciality
            List<ModuleName> limitedModules = Arrays.asList(ModuleName.values()).subList(0, 2);
            for (ModuleName moduleName : limitedModules) {
                Module module = new Module(null, moduleName, null, speciality);
                module = moduleRepository.save(module);

                // Create 2 groups for each module
                for (int i = 1; i <= 1; i++) {
                    Groupe groupe = new Groupe((Long) null, "Group " + i, 500.0, 30, "Sunday", 5, 3, GroupeStatus.ACTIVE, "https://placehold.co/600x400", null, module, module.getName() == ModuleName.ARABIC ? 2L : null, null, null);
                    groupe = groupeRepository.save(groupe);
                    if(module.getName() == ModuleName.ARABIC){
                        statisticsProxy.addGroupe(groupe.getIdGroupe(), groupe.getName(), groupe.getIdTeacher());
                    }

                    // Get the next two Sundays
                    List<Date> nextTwoSundays = getNextTwoSundays();

                    // Create 2 lectures for each group
                    for (int j = 1; j <= 1; j++) {
                        Lecture lecture = new Lecture();
                        lecture.setTitle("Lecture " + j);
                        lecture.setDate(nextTwoSundays.get(j - 1));
                        lecture.setGroupe(groupe);
                        lecture.setDocumentList(Arrays.asList(document1, document2));
                        lecture = lectureRepository.save(lecture);

                        Conference conference = new Conference();
                        conference.setLink("http://example.com/conference");
                        conference.setDuration("1 hour");
                        conference.setLecture(lecture);
                        conference = conferenceRepository.save(conference);

                        lecture.setConference(conference);
                        Lecture l = lectureRepository.save(lecture);
                        if(module.getName() == ModuleName.ARABIC && j <= 2) {
                            statisticsProxy.addLecture(l.getIdLecture(), l.getTitle(), groupe.getLecturePrice(), groupe.getIdGroupe());
                        }
                    }
                }
            }
        }
    }

    private List<Date> getNextTwoSundays() {
        // Helper method to get the next two Sundays
        List<Date> sundays = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        while (sundays.size() < 2) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                sundays.add(calendar.getTime());
            }
            calendar.add(Calendar.DATE, 1);
        }
        return sundays;
    }

    private List<Date> getNextFiveSundays() {
        Calendar calendar = Calendar.getInstance();
        // Move calendar to the next Sunday
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        // Generate the next five Sundays
        return IntStream.range(0, 5)
                .mapToObj(i -> {
                    Calendar c = (Calendar) calendar.clone();
                    c.add(Calendar.DAY_OF_WEEK, i * 7);
                    return c.getTime();
                })
                .collect(Collectors.toList());
    }
}
