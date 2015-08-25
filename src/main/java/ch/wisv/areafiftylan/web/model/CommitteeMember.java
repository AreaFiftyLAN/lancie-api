package ch.wisv.areafiftylan.web.model;

public class CommitteeMember {

    String name;
    String function;
    String icon;

    public CommitteeMember(String name, String function, String icon) {
        this.name = name;
        this.function = function;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
