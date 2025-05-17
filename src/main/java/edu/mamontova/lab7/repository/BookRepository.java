package edu.mamontova.lab7.repository;/*
  @author tanus
  @project lab7
  @class BookRepository
  @version 1.0.0
  @since 02.05.2025 - 13.44
*/
import edu.mamontova.lab7.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends MongoRepository<Book, String>  {
    public boolean existsByCode(String code);
}