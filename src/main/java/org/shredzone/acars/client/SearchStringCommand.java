package org.shredzone.acars.client;

/**
 * A {@link StringCommand} that also contains a search string.
 *
 * @author Richard "Shred" Körber
 */
public class SearchStringCommand extends StringCommand {

    private final String search;

    public SearchStringCommand(Operation op, String value, String search) {
        super(op, value);
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

}
