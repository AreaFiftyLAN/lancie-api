package ch.wisv.areafiftylan.web.model;

public class Sponsor {

    String name;
    String imagePath;
    String website;

    public Sponsor(String name, String imagePath, String website) {
        this.name = name;
        this.imagePath = imagePath;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
