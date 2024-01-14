package nl.tudelft.sem.template.authentication.handlers;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;

abstract class AbstractHandler implements Handler {
    @Getter
    private transient UserRepository userRepository;
    @Getter
    private transient Handler nextHandler;
    @Getter
    @Setter
    private transient Strategy strategy;

    /**
     * Used by children of AbstractHandler object.
     *
     * @param userRepository is the UserRepository.
     */
    public AbstractHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }

    @Override
    public abstract void filter(FilterBookRequestModel filterBookRequestModel);

    protected Handler getHandler() {
        return this.nextHandler;
    }
}
