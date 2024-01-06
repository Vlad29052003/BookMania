package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserWasCreatedListenerTests {
//    private final transient UserWasCreatedListener userWasCreatedListener = new UserWasCreatedListener();
    private final transient ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private transient UserWasCreatedEvent userWasCreatedEvent;

//    @BeforeEach
//    public void setUp() {
//        System.setOut(new PrintStream(outputStreamCaptor));
//        this.userWasCreatedEvent = new UserWasCreatedEvent(new Username("user"));
//    }
//
//    @Test
//    public void onAccountWasCreated() {
//        userWasCreatedListener.onAccountWasCreated(userWasCreatedEvent);
//        assertThat(outputStreamCaptor.toString().trim())
//                .isEqualTo("Account (user) was created.");
//    }
//
//    @AfterEach
//    public void revertChanges() {
//        System.setOut(System.out);
//    }
}
