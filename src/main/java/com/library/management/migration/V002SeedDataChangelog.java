package com.library.management.migration;

import com.library.management.book.model.BookDocument;
import com.library.management.config.SeedProperties;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@ChangeUnit(id = "v002-seed-admin-and-books", order = "002", author = "library-starter")
public class V002SeedDataChangelog {

    @Execution
    public void execution(
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder,
            SeedProperties seedProperties
    ) {
        seedAdmin(mongoTemplate, passwordEncoder, seedProperties);
        seedBooks(mongoTemplate);
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate, SeedProperties seedProperties) {
        mongoTemplate.remove(Query.query(Criteria.where("email").is(seedProperties.adminEmail())), UserDocument.class);
        mongoTemplate.remove(Query.query(Criteria.where("isbn").in(
                "9780134685991",
                "9780596009205",
                "9781492078005"
        )), BookDocument.class);
    }

    private void seedAdmin(
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder,
            SeedProperties seedProperties
    ) {
        Query existingAdmin = Query.query(Criteria.where("email").is(seedProperties.adminEmail().toLowerCase()));
        if (mongoTemplate.exists(existingAdmin, UserDocument.class)) {
            return;
        }

        Instant now = Instant.now();
        UserDocument admin = new UserDocument();
        admin.setEmail(seedProperties.adminEmail().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(seedProperties.adminPassword()));
        admin.setFullName(seedProperties.adminFullName());
        admin.setRoles(Set.of(Role.ADMIN));
        admin.setEnabled(true);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        mongoTemplate.insert(admin);
    }

    private void seedBooks(MongoTemplate mongoTemplate) {
        List<BookDocument> samples = List.of(
                book("Effective Java", "Joshua Bloch", "9780134685991",
                        "Best practices for the Java platform.", 2018, 5, 5),
                book("Head First Design Patterns", "Eric Freeman", "9780596009205",
                        "A brain-friendly guide to design patterns.", 2004, 3, 3),
                book("Designing Data-Intensive Applications", "Martin Kleppmann", "9781492078005",
                        "The big ideas behind reliable, scalable data systems.", 2017, 4, 4)
        );

        for (BookDocument sample : samples) {
            Query byIsbn = Query.query(Criteria.where("isbn").is(sample.getIsbn()));
            if (!mongoTemplate.exists(byIsbn, BookDocument.class)) {
                mongoTemplate.insert(sample);
            }
        }
    }

    private BookDocument book(
            String title,
            String authorName,
            String isbn,
            String description,
            int year,
            int totalCopies,
            int availableCopies
    ) {
        Instant now = Instant.now();
        BookDocument document = new BookDocument();
        document.setTitle(title);
        document.setAuthorName(authorName);
        document.setIsbn(isbn);
        document.setDescription(description);
        document.setPublicationYear(year);
        document.setTotalCopies(totalCopies);
        document.setAvailableCopies(availableCopies);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return document;
    }
}
