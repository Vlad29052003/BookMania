package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;

public interface Handler {
    void setNext(Handler handler);

    void filter(FilterBookRequestModel filterBookRequestModel);
}
