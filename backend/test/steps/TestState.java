package steps;


import models.Role;
import play.Application;
import utils.FakeClient;

public class TestState {

    private Application application;
    private static TestState testState;
    private FakeClient fakeClient;
    private Role superAdminRole;
    private Role adminRole;
    private Role travellerRole;

    /**
     * Get an instance of this class, creates it if necessary.
     *
     * @return the instance of this class.
     */
    public static TestState getInstance() {
        if (testState == null) {
            testState = new TestState();
        }
        return testState;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public FakeClient getFakeClient() {
        return fakeClient;
    }

    public void setFakeClient(FakeClient fakeClient) {
        this.fakeClient = fakeClient;
    }

    public Role getSuperAdminRole() {
        return superAdminRole;
    }

    public void setSuperAdminRole(Role superAdminRole) {
        this.superAdminRole = superAdminRole;
    }

    public Role getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Role adminRole) {
        this.adminRole = adminRole;
    }

    public Role getTravellerRole() {
        return travellerRole;
    }

    public void setTravellerRole(Role travellerRole) {
        this.travellerRole = travellerRole;
    }
}
