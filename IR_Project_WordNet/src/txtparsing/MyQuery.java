package txtparsing;

/**
 * @author Dimitrios Stefanou
 */

public class MyQuery {

    private String queryId;
    private String queryText;

    public MyQuery(String queryId, String queryText) {
        this.setQueryId(queryId);
        this.setQueryText(queryText);
    }

    @Override
    public String toString() {
        String ret = "MyQuery{"
                + "\n\tID: " + queryId
                + "\n\tText: " + queryText;
        return ret + "\n}";
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
}
