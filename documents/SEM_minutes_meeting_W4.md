# Minutes Meeting 05/12/23

**Location:** Flux Hall A

**Date:** 28/11/2023

**Time:** 14:00-16:00

**Group:** 18c

**Meeting participants:**

- Vlad Alexan

- Vlad Graure

- Simona Lupsa

- Prisha Meswani

- Andrei Stefan (minute-taker)

- Charlene Bakker (chair)

**Talking points**

- [14:00] Opening by Chair.
- [14:00-14:02] Approval of agenda: Does anyone have any last-minute additions?
- [14:02-14:40] Giving feedback on other API specifications
  - We discussed together and gave feedback on the API specifications of the other teams, taking into account the requirements of the project and our collaboration.
- [14:40-14:50] Discuss the progress of the first sprint iteration and talk about any issues each teammate may have.
  - We discussed together about issues each of us encountered and discussed about how to fix them shortly.
- [15:00-15:30] Checked TA feedback given on our API specification and discussed how to change it to meet the requirements and [resolve the issues described](#decisions).
- [15:30-16:00] Discuss with other teams and TAs about the API specifications and decide on [various endpoint changes](#meeting-with-other-teams--tas).
- [16:00] Closure by Chair

#### Decisions

We will adjust the API specification to meet the requirements and resolve the issues described by both the teams and the SEM staff.

- Added the server URL to the API specification.
- Removed the token requirement from the authentication endpoint.
- Add endpoints to retrieve followers and following users.
- Add more detailed examples to the API specification endpoints.
- When considering things like searching for a user, now we return a special class that doesn't contain sensitive information like the password.

#### Meeting with other teams & TAs

We discussed with the other teams and the lecturer about the API specifications and decided to keep the Spring Security Filter.

To give the other microservice teams the choice whether to use the filter or not, we will create new endpoints which are unprotected by the Spring Security Filter, so that the other teams can access them without checking authorities.
