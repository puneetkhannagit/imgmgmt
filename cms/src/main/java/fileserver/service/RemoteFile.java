package fileserver.service;

public class RemoteFile {
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public RemoteFile(String location) {
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String location;
    private int id;
}
