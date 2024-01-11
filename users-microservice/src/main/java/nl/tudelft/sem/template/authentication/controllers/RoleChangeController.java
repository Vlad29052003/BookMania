package nl.tudelft.sem.template.authentication.controllers;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChange;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChangeService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/c/roleChangeRequests")
public class RoleChangeController {

    private final transient RoleChangeService roleChangeService;

    /**
     * Creates a RoleChangeController.
     *
     * @param roleChangeService role change service.
     */
    @Autowired
    public RoleChangeController(RoleChangeService roleChangeService) {
        this.roleChangeService = roleChangeService;
    }

    /**
     * GET request for admins. Retrieves all requests from the DB.
     *
     * @return list of all requests.
     */
    @GetMapping("")
    public ResponseEntity<List<RoleChange>> getAllRequests() {
        String authority = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .get(0).getAuthority();
        return ResponseEntity.ok(roleChangeService.getAll(authority));
    }

    /**
     * POST request. A user submits a role change requests (i.e. adds it to the DB).
     *
     * @param request role change request.
     * @return 200 OK if the request was submitted successfully; errors otherwise.
     */
    @PostMapping("")
    public ResponseEntity<Void> addRoleChangeRequest(@RequestBody RoleChange request) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        roleChangeService.addRequest(username, request);
        return ResponseEntity.ok().build();
    }
}
