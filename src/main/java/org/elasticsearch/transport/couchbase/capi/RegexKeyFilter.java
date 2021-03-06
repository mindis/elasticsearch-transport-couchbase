package org.elasticsearch.transport.couchbase.capi;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexKeyFilter implements KeyFilter {

    protected ESLogger logger = Loggers.getLogger(getClass());
    private String keyFilterType;
    private Map<String,String> keyFilterPatternStrings;
    private Map<String, Pattern> keyFilterPatterns;

    @Override
    public void configure(Settings settings) {
        this.keyFilterType = settings.get("couchbase.keyFilterType", DefaultKeyFilter.DEFAULT_KEY_FILTER_TYPE);
        logger.trace("Using key filter type: {}", keyFilterType);
        this.keyFilterPatterns = new HashMap<String,Pattern>();
        this.keyFilterPatternStrings = settings.getByPrefix("couchbase.keyFilters.").getAsMap();
        for (String key : keyFilterPatternStrings.keySet()) {
            String pattern = keyFilterPatternStrings.get(key);
            logger.trace("See key filter: {} with pattern: {} compiling...", key, pattern);
            keyFilterPatterns.put(key, Pattern.compile(pattern));
        }
    }


    @Override
    public Boolean shouldAllow(String index, String docId) {
        Boolean matches = matchesAnyFilter(index, docId);
        if(keyFilterType.toLowerCase().equals("include"))
            return matches;
        else
            return !matches;
    }

    private Boolean matchesAnyFilter(String index, String docId) {
        Boolean include = false;

        for(Map.Entry<String,Pattern> typePattern : this.keyFilterPatterns.entrySet()) {
            include = include || typePattern.getValue().matcher(docId).matches();
        }

        return include;
    }
}
