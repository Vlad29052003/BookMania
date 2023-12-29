package nl.tudelft.sem.template.authentication.filters;

import lombok.Setter;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;

abstract class AbstractHandler implements Handler {
    private transient Handler nextHandler;
    @Setter
    private transient Strategy strategy;

    @Override
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }

    @Override
    public abstract void filter(FilterBookRequestModel filterBookRequestModel);

    protected Handler getHandler() {
        return this.nextHandler;
    }

    protected Strategy getStrategy() {
        return this.strategy;
    }
}
