package nl.tudelft.sem.template.authentication.application.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

/**
 * This event listener is automatically called when an user entity is saved
 * which has stored events of type: UserWasCreated.
 */
@Component
public class UserWasCreatedListener {

    private MockMvc mockMvc;

    /**
     * On account creation call the endpoint of the bookshelves microservice to create an user.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onAccountWasCreated(UserWasCreatedEvent event) {
        try {
            mockMvc.perform(post("http://localhost:8081/a/user")
                    .contentType("application/json")
                    .content(event.getId().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
