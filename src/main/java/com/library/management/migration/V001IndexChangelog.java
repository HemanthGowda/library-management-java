package com.library.management.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(id = "v001-create-indexes", order = "001", author = "library-starter")
public class V001IndexChangelog {

    @Execution
    public void execution(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps("users").ensureIndex(
                new Index().on("email", Sort.Direction.ASC).unique().named("uk_users_email")
        );

        mongoTemplate.indexOps("books").ensureIndex(
                new Index().on("isbn", Sort.Direction.ASC).unique().named("uk_books_isbn")
        );

        mongoTemplate.indexOps("books").ensureIndex(
                new Index().on("title", Sort.Direction.ASC).named("idx_books_title")
        );
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps("users").dropIndex("uk_users_email");
        mongoTemplate.indexOps("books").dropIndex("uk_books_isbn");
        mongoTemplate.indexOps("books").dropIndex("idx_books_title");
    }
}
