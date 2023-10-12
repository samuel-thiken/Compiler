package fr.ensimag.deca.tree;

import java.io.PrintStream;

/**
 * Exception corresponding to an error at a particular location in a file.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class LocationException extends Exception {
    public Location getLocation() {
        return location;
    }

    public void display(PrintStream s) {
        s.println(getMessageWithLocation());
    }

    private static final long serialVersionUID = 7628400022855935597L;
    protected Location location;

    public LocationException(String message, Location location) {
        super(message);
        assert(location == null || location.getFilename() != null);
        this.location = location;
    }

    public String getMessageWithLocation() {
        Location loc = getLocation();
        String line;
        String column;
        if (loc == null) {
            line = "<unknown>";
            column = "";
        } else {
            line = Integer.toString(loc.getLine());
            column = ":" + loc.getPositionInLine();
        }
        return location.getFilename() + ":" + line + column + ": " + getMessage();
    }

}
