package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.strategy.Strategy;

abstract class AbstractHandler implements Handler {
    private transient Handler nextHandler;
    private transient Strategy strategy;

    @Override
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }

    @Override
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public abstract void filter(Book book, String bearerToken);

    protected Handler getHandler() {
        return this.nextHandler;
    }

    protected Strategy getStrategy() {
        return this.strategy;
    }
}
