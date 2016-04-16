package ch.wisv.areafiftylan.web.model;

public class WebEvent {

    String title;
    String subtitle;
    String headerTitle;
    String description;
    String backgroundImagePath;

    public WebEvent(String title, String subtitle, String headerTitle, String description, String backgroundImagePath) {
        this.title = title;
        this.subtitle = subtitle;
        this.headerTitle = headerTitle;
        this.description = description;
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }
}
