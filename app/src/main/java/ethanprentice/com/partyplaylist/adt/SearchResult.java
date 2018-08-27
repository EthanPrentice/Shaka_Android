package ethanprentice.com.partyplaylist.adt;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    public TypeResult<Track> tracks;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeResult<T> {
        public String previous;
        public String next;

        public int limit;
        public int offset;

        public T[] items;
    }
}
