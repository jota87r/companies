package com.example.companies;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import static javax.persistence.FetchType.EAGER;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class CompaniesApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompaniesApplication.class, args);
    }
}

@RestController
@RequestMapping("companies")
class CompanyController {
    @Autowired CompanyService service;
    @Autowired GreetingService greetingService;
    @GetMapping("/{id}")
    public Optional<Company> get(@PathVariable long id) {
        return service.findById(id);
    }
    @PostMapping
    public Company post(@RequestBody Company company) {
        return service.save(company);
    }
    @GetMapping("/ceo/{ceo}")
    public List<Company> findByCeo(@PathVariable String ceo) {
        return service.findByCeo(ceo);
    }
    @GetMapping("greeting/{name}")
    public String greeting(@PathVariable String name) {
        String greeting = greetingService.greeting(name);
        return greeting;
    }
}

@Component
@Aspect
class TimmingAOP {
    @Pointcut("within(com.example.companies..*)")
    public void inDemoPackage() {}
    @Pointcut("execution(public * *(..))")
    public void isPublic() {}
    @Around("inDemoPackage() && isPublic()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = joinPoint.proceed();
        long end = System.currentTimeMillis();
        System.out.println("duration in millis: " + (end - start));
        return obj;
    }
}

interface GreetingService {
    String greeting(String name) ;
}

@Service
@Profile("eng")
class EnglishGreetingService implements GreetingService {
    @Override public String greeting(String name) {
        return "hello, " + name;
    }
}

@Service
@Profile("esp")
class SpanishGreetingService implements GreetingService {
    @Override public String greeting(String name) {
        return "hola, " + name;
    }
}

@Service
class CompanyService {
    @Autowired CompanyRepository companyRepository;
    public Optional<Company> findById(long id) {
        return companyRepository.findById(id);
    }
    @Transactional
    public Company save(Company company) {
        Objects.requireNonNull(company.getCeo(), "CEO is mandatory!!");
        return companyRepository.save(company);
    }
    public List<Company> findByCeo(String ceo) {
        return companyRepository.findByCeoName(ceo);
    }
    @Scheduled(cron = "*/10 * * * * *")
    public void runEveryTenSeconds() {
        System.out.println("10 seconds have passed");
    }
}

@Data //lombok
@NoArgsConstructor //lombok
@AllArgsConstructor //lombok
@Entity
class Company {
    @Id @GeneratedValue private long id;
    String name;
    @OneToOne(fetch = EAGER, cascade = CascadeType.PERSIST) Person ceo;
}

@Data //lombok
@NoArgsConstructor //lombok
@AllArgsConstructor //lombok
@Entity
class Person {
    @Id String id;
    String name;
}

@Repository
interface CompanyRepository extends CrudRepository<Company, Long> {
    List<Company> findByCeoName(String ceo);
    @Query("SELECT c FROM Company c where c.ceo.id = :personId")
    List<Company> get(String personId);
}