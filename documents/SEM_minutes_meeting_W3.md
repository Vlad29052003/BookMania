# Minutes Meeting 28/11/23

**Location:** Flux Hall A
**Date:** 28/11/2023
**Time:** 14:00-16:00
**Group:** 18c
**Meeting participants:**

-   Vlad Alexan
-   Vlad Graure (minute-taker)
-   Simona Lupsa
-   Prisha Meswani (chair)
-   Andrei Stefan
-   Charlene Bakker

**Talking points**
* [14:00] Opening by Chair.
* [14:00-14:02] Approval of agenda: Does anyone have any last-minute additions?
* [14:02-14:20] Finalizing the OpenAPI Specifications.
    * Check for any changes/corrections to be made.
    * Last minute additions, if any.
* [14:20-14:35] Work on Task 3 of the W3 objectives.
    * Write descriptions of each member's assigned feature.
* [14:35-14:40] Discuss the integration of the microservices.
    * Verify the other teams have the services required for the communication of our services to theirs.
* [14:40-14:50] Discuss the progress of the first sprint itertation.
* [14:50-14:58] Update our GitLab Wiki according to the Lab Manual clause 5.6.
* [14:58-15:00] Ensure we have completed everything for this week's mandatory deadline.
* [15:00] Closure by Chair

**Decisions**

- We do not consider the deactivation. Only account deletion and ban by admin.
- For banned accounts, they will have a boolean set to true (isBanned)
- A banned user cannot log in anymore (should be checked in the jwt if the user is not banned)
- Add a number of pages field to the book
- All users will start as regular users. They can request to change to author or admin, and they need to provide SSN and real name in order to do so. This will be checked and approved/denied by an admin.
- The system will have a default admin.
- We need to notify other microservices of User deletions.

**Notes:**

- We should meet on Thursday to finalise the OpenAPI Specifications
- The open api is generally good but might need some improvements - better descriptions and examples

**Meething with other teams**

- For the Comment and Review microservice
  - we need en endpoint to notify when a user is deleted
  - they need an endpoint to send us ban requests
  - we need an endpoint to tell them what happened with the ban request (accepted/deleted)
- For the Bookshelf microservice:
  - we need endpoint to notify of user created/deleted
  - we need endpoint to notify of book created/edited/deleted
