package nl.tudelft.sem.template.authentication.models;

import java.util.List;
import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.book.Genre;

@Data
public class CreateBookRequestModel {
    private String title;
    private List<String> authors;
    private List<Genre> genres;
    private String description;
    private int numPages;
}
