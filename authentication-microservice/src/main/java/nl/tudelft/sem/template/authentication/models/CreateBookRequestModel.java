package nl.tudelft.sem.template.authentication.models;

import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import java.util.List;

@Data
public class CreateBookRequestModel {
    private String id;
    private String title;
    private List<String> authors;
    private List<Genre> genre;
    private String description;
    private int numPages;
}
