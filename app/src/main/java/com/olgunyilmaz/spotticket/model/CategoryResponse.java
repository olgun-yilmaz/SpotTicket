package com.olgunyilmaz.spotticket.model;

public class CategoryResponse {
    private int imageID;

    public CategoryResponse(int imageID, String categoryName) {
        this.imageID = imageID;
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    private String categoryName;
}
